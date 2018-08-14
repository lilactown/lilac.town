(ns lilactown.client.sweeper
  (:require [lilactown.react :as r]
            [lilactown.react.dom :as dom]
            [lilactown.css :as css]
            [lilactown.cursor :as cursor]))

;; State

(defn initial-state [size mines]
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

(defonce grid-state (atom (initial-state 15 80)))

(defn neighbors [row col]
  (let [grid @grid-state]
    [(grid [(inc row) col])
     (grid [(inc row) (inc col)])
     (grid [row (inc col)])
     (grid [(dec row) col])
     (grid [(dec row) (dec col)])
     (grid [row (dec col)])
     (grid [(inc row) (dec col)])
     (grid [(dec row) (inc col)])]))


;; Events

(defn clear-square! [row col]
  (swap! grid-state update [row col] assoc :cleared? true))

(defn mark-square! [row col]
  (swap! grid-state update [row col] assoc :marked? true))

(defn reset-grid! [size mines]
  (reset! grid-state (initial-state size mines)))


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
    :cursor "pointer"}))

(def cleared-style
  (css/edn
   square-style
   {:box-shadow "0 0 0"
    :cursor "default"}))

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
            :className (str (if cleared?
                              cleared-style
                              square-style) " "
                            (when (not cleared?)
                              hover-buzz-style))
            :onClick (when (not cleared?)
                       #(clear-square! row col))
            :onContextMenu (when (not cleared?)
                             #(do
                                (. % preventDefault)
                                (mark-square! row col)))}
           (case [cleared? marked? explodes?]
             ([false false false]
              [false false true]) nil
             ([false true false]
              [false true true]) "?"
             ([true false false]
              [true true false]) (count (filter :explodes?
                                                (neighbors row col)))
             ([true false true]
              [true true true] "*"))))

(r/defnc Grid [{state :state}]
  (dom/div
   (dom/div {:style #js {:padding "20px 0"}}
            (dom/button {:onClick #(reset-grid! 15 80)} "Reset"))
   (dom/div {:style #js {:display "grid"
                         :gridGap "5px"}}
            ;; keys are [col row], value is square state
            (for [[[row col] square] state]
              (Square (assoc square
                             :key [row col]
                             :col col
                             :row row))))))


;; Hook up to state

(r/defrc Container
  {:watch (fn [_] {:state grid-state})}
  [_ {state :state}]
  (Grid {:state @state}))
