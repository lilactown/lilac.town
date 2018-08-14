(ns lilactown.client.sweeper
  (:require [lilactown.react :as r]
            [lilactown.react.dom :as dom]
            [lilactown.css :as css]
            [react-dom :as react-dom]))

;; State

(defn initial-grid [size mines]
  ;; convert into a map so we can navigate the grid cheaply
  (into
   {}
   (for [[i square] (-> (- (* size size) mines) ;; size of grid minus the number of mines
                        (repeat {:explodes? false}) ;; map them to squares that don't explode
                        (into (repeat mines {:explodes? true})) ;; add squares that do explode
                        (shuffle) ;; shuffle all of them so that they're in random order
                        ;; associate the index with each square [i square]
                        (as-> l (map-indexed vector l)))]
     ;; we add 1 since CSS grid wants 1..n
     (let [row (inc (quot i size))
           col (inc (mod i size))]
       [[row col] (assoc square
                         :marked? false
                         :cleared? false)]))))

(def sweeper-state (atom {:size 15
                          :mines 80
                          :grid (initial-grid 15 80)}))

(defn neighbors [row col]
  (let [grid (:grid @sweeper-state)]
    [(grid [(inc row) col])
     (grid [(inc row) (inc col)])
     (grid [row (inc col)])
     (grid [(dec row) col])
     (grid [(dec row) (dec col)])
     (grid [row (dec col)])
     (grid [(inc row) (dec col)])
     (grid [(dec row) (inc col)])]))

(defn clear-grid [grid]
  (reduce-kv
   (fn [m k sq]
     (assoc m k (assoc sq :cleared? true)))
   {}
   grid))


;; Events

(defn clear-square! [row col]
  (swap! sweeper-state update-in [:grid [row col]] assoc :cleared? true))

(defn mark-square! [row col]
  (swap! sweeper-state update-in [:grid [row col]] assoc :marked? true))

(defn unmark-square! [row col]
  (swap! sweeper-state update-in [:grid [row col]] assoc :marked? false))

(defn explode-square! [row col]
  (swap! sweeper-state update :grid clear-grid))

(defn reset-grid! [size mines]
  (swap! sweeper-state update :grid #(initial-grid size mines)))

(defn update-size! [size]
  (swap! sweeper-state assoc :size size))

(defn update-mines! [mines]
  (swap! sweeper-state assoc :mines mines))


;; Styles

(def square-style
  (css/edn
   {:width "30px"
    :height "30px"
    :background-color "rgb(187, 164, 255)"
    :color "white"
    :font-family "sans-serif"
    :display "flex"
    :justify-content "center"
    :align-items "center"
    :box-shadow "2px 2px 3px rgba(100, 100, 100, .5)"
    :cursor "pointer"
    :border-radius "3px"}))

(def cleared-style
  (css/edn
   square-style
   {:box-shadow "0 0 0"
    :cursor "default"}))

(def marked-style
  (css/edn
   square-style
   {:background-color "rgb(214, 200, 255)"}))

(def hover-buzz-animation
  (css/keyframes
   "50% {
      transform: translateX(3px) rotate(2deg)
   }
   100% {
     transform: translateX(-3px) rotate(-2deg)
   }"))

(def hover-buzz-style
  (css/edn
   {"&:hover, &:focus, &:active"
    {:animation-name hover-buzz-animation
     :animation-duration "0.15s"
     :animation-timing-function "linear"
     :animation-iteration-count "infinite"}}))


;; Components

(r/defnc Square
  [{:keys [col row explodes? marked? cleared?]}]
  (dom/div {:style #js {:gridColumn col
                        :gridRow row
                        :backgroundColor (when (and cleared? explodes?)
                                           "black")}
            :className (case [cleared? marked?]
                         [false false] (str square-style " " hover-buzz-style)
                         [false true] (str marked-style " " hover-buzz-style)
                         ([true false]
                          [true true]) cleared-style)
            :onClick (case [cleared? explodes?]
                       [false true] #(explode-square! row col)
                       [false false] #(clear-square! row col)
                       nil)
            :onContextMenu (when (not cleared?)
                             #(do
                                (. % preventDefault)
                                (if marked?
                                  (unmark-square! row col)
                                  (mark-square! row col))))}
           (case [cleared? marked? explodes?]
             ([false false false]
              [false false true]) nil
             ([false true false]
              [false true true]) "?"
             ([true false false]
              [true true false]) (count (filter :explodes?
                                                (neighbors row col)))
             ([true false true]
              [true true true] "✸"))))

(r/defnc Grid [{state :state}]
  (dom/div {:style #js {:display "grid"
                        :gridGap "5px"
                        :gridAutoColumns "min-content"
                        :justifyContent "center"}}
           ;; keys are [col row], value is square state
           (for [[[row col] square] state]
             (Square (assoc square
                            :key [row col]
                            :col col
                            :row row)))))


;; Hook up to state

(r/defrc Container
  {:watch (fn [_] {:state sweeper-state})}
  [_ {state :state}]
  (dom/div
   (dom/div {:style #js {:padding "20px 0"
                         :fontFamily "sans-serif"
                         :display "flex"
                         :justifyContent "center"}}
            (dom/div
             {:style #js {:padding "0 10px"}}
             "Size: "
             (dom/input {:type "number"
                         :style #js {:width "50px"}
                         :value (:size @state)
                         :onChange
                         #(update-size! (js/parseInt
                                         (.. % -target -value)))}))
            (dom/div
             {:style #js {:padding "0 10px"}}
             "Mines: "
             (dom/input {:type "number"
                         :style #js {:width "50px"}
                         :value (:mines @state)
                         :onChange
                         #(update-mines! (js/parseInt
                                          (.. % -target -value)))}))
            (dom/button {:onClick #(reset-grid! (:size @state)
                                                (:mines @state))} "Reset"))
   (Grid {:state (:grid @state)})))

(defn ^:export start! [node]
  (react-dom/render (Container) node))
