(ns lilactown.client.sweeper
  (:require [lilactown.react :as r]
            [lilactown.react.dom :as dom]
            [lilactown.css :as css]
            [react-dom :as react-dom]))

;; State

(def ^:const difficulties
  {:Easy {:width 10 :height 10 :mines 10}
   :Medium {:width 16 :height 16 :mines 40}
   :Hard {:width 16 :height 30 :mines 99}})

(defn initial-grid [width height mines]
  ;; convert into a map so we can navigate the grid cheaply
  (into
   {}
   (for [[i square] (-> (- (* width height) mines) ;; size of grid minus the number of mines
                        (repeat {:explodes? false}) ;; map them to squares that don't explode
                        (into (repeat mines {:explodes? true})) ;; add squares that do explode
                        (shuffle) ;; shuffle all of them so that they're in random order
                        ;; associate the index with each square [i square]
                        (as-> l (map-indexed vector l)))]
     ;; we add 1 since CSS grid wants 1..n
     (let [row (inc (quot i width))
           col (inc (mod i width))]
       [[row col] (assoc square
                         :marked? false
                         :cleared? false)]))))

(defonce sweeper-state (atom {:width 10
                              :height 10
                              :mines 10
                              :wiggle? true
                              :grid (initial-grid 10 10 10)}))


(defn neighbors [grid row col & {:keys [four-connected?]
                                 :or {four-connected? false}}]
  (concat
   [[(inc row) col]
    [row (inc col)]
    [(dec row) col]
    [row (dec col)]]
   (when (not four-connected?)
     [[(inc row) (inc col)]
      [(dec row) (dec col)]
      [(inc row) (dec col)]
      [(dec row) (inc col)]])))

(defn clear-grid [grid]
  (reduce-kv
   (fn [m k sq]
     (assoc m k (assoc sq :cleared? true)))
   {}
   grid))

(defn count-mine-neighbors [grid row col]
  (->> (neighbors grid row col)
       (map grid)
       (filter :explodes?
               )
       (count )))

(defn has-won? [grid]
  (->> grid
       (map second)
       (filter (comp not :cleared?))
       (every? :explodes?)))

(defn has-lost? [grid]
  (->> grid
       (map second)
       (filter :explodes?)
       (some :cleared?)))

(defn safe? [grid row col]
  (= (count-mine-neighbors grid row col) 0))

(defn flood-fill [grid row col]
  ;; flood fill algorithm
  (if (and (grid [row col]) ;; inside grid?
           (not (:visited? (grid [row col]))))
    (if (safe? grid row col)
      (let [visited (assoc-in grid [[row col] :visited?] true) ;; mark current as visited
            neighbors (neighbors grid row col :four-connected? true)] ;; get all neighbors
        (-> (reduce (fn [g n]
                      ;; apply flood-fill to the newly visited grid for each neighbor
                      (flood-fill g (first n) (second n)))
                    visited
                    neighbors)))
      ;; if not safe, we mark it as visited and return
      (assoc-in grid [[row col] :visited?] true))
    ;; if either not inside grid or has already been visited, still return the grid
    grid))

(defn all-safe-neighbors [grid row col]
  (->> (flood-fill grid row col)
       ;; get only the visited squares
       (filter (comp :visited? second))
       ;; get their coordinates
       (map first)))

#_(count (all-safe-neighbors (:grid @sweeper-state) 10 1))


;; Events

(defn clear-square! [row col]
  (swap! sweeper-state
         (fn [state]
           (if (safe? (:grid state) row col)
             (->> (reduce (fn [g n]
                            (assoc-in g [n :cleared?] true))
                          (:grid state)
                          (all-safe-neighbors (:grid state) row col))
                  (assoc state :grid))

             (assoc-in state [:grid [row col] :cleared?] true)))))

(defn mark-square! [row col]
  (swap! sweeper-state update-in [:grid [row col]] assoc :marked? true))

(defn unmark-square! [row col]
  (swap! sweeper-state update-in [:grid [row col]] assoc :marked? false))

(defn explode-square! [row col]
  (swap! sweeper-state update :grid clear-grid))

(defn reset-grid! [width height mines]
  (swap! sweeper-state update :grid #(initial-grid width height mines)))

(defn update-size! [width height]
  (swap! sweeper-state assoc
         :width width
         :height height))

(defn update-difficulty! [width height mines]
  (swap! sweeper-state assoc
         :width width
         :height height
         :mines mines))

(defn update-mines! [mines]
  (swap! sweeper-state assoc :mines mines))

;; Styles

(def square-style
  (css/edn
   {:width "30px"
    :height "30px"
    :background-color "rgb(187, 164, 255)"
    :color "#3b3b3b"
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

(def exploded-style
  (css/edn
   cleared-style
   {:color "white"
    :background-color "black"}))

(def hover-bob-animation
  (css/keyframes
   "0% {
    transform: translateY(-2px);
  }
  50% {
    transform: translateY(0px);
  }
  100% {
    transform: translateY(-2px);
  }"))

(def hover-bob-float-animation
  (css/keyframes
   "100% {
    transform: translateY(-2px);
  }"))

(def hover-bob-style
  (css/edn
   {"&:hover, &:focus, &:active"
    {:animation-name (str hover-bob-float-animation ", " hover-bob-animation)
     :animation-duration ".3s, 1.5s"
     :animation-delay "0s, .3s"
     :animation-timing-function "ease-out, ease-in-out"
     :animation-iteration-count "1, infinite"
     :animation-fill-mode "forwards"
     :nimation-direction "normal, alternate"}}))


;; Components

(r/defnc Square
  [{:keys [col row explodes? marked? cleared?]}]
  (let [mine-count (count-mine-neighbors (:grid @sweeper-state) row col)]
    (dom/div {:style #js {:gridColumn col
                          :gridRow row
                          :backgroundColor (when (and cleared? (not explodes?))
                                             (case mine-count
                                               0 "#5cc75c"
                                               1 "#a0d5ef"
                                               2 "#8484ff"
                                               3 "#ffdc33"
                                               4 "orange"
                                               5 "#ff8100"
                                               6 "#ff7878"
                                               7 "#ff4d4d"
                                               8 "red"))}
              :className (case [cleared? marked?]
                           [false false]
                           (str square-style " " hover-bob-style)
                           [false true]
                           (str marked-style " " hover-bob-style)
                           ([true false]
                            [true true]) (if explodes?
                                           exploded-style
                                           cleared-style))
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
                [true true false]) mine-count
               ([true false true]
                [true true true] "✸")))))

(r/defnc Grid [{state :state}]
  (dom/<>
   (dom/div
    {:style #js {:textAlign "center"
                 :padding "10px"
                 :height "40px"}}
    (cond
      (has-lost? state)
      "You lost."
      (has-won? state)
      "You won!"
      :else ""))
   (dom/div {:style #js {:display "grid"
                         :gridGap "3px"
                         :gridAutoColumns "min-content"
                         :justifyContent "center"}}
            ;; keys are [col row], value is square state
            (for [[[row col] square] state]
              (Square (assoc square
                             :key [row col]
                             :col col
                             :row row))))))


;; Hook up to state

(r/defrc Container
  {:watch (fn [_] {:state sweeper-state})}
  [_ {state :state}]
  (dom/div
   (dom/div {:style #js {:padding "20px 0"
                         :fontFamily "sans-serif"
                         :display "flex"
                         :justifyContent "center"}}
            (dom/div {:style #js {:padding "0 10px"}}
                     "Difficulty: "
                     (dom/select
                      {:onChange
                       (fn [ev]
                         (let [difficulty (keyword (.. ev -target -value))
                               {:keys [width
                                       height
                                       mines]} (difficulties difficulty)]
                           (update-difficulty! width height mines)))}
                      (map #(dom/option (name (first %))) difficulties)))
            ;; (dom/div
            ;;  {:style #js {:padding "0 10px"}}
            ;;  "Mines: "
            ;;  (dom/input {:type "number"
            ;;              :style #js {:width "50px"}
            ;;              :value (:mines @state)
            ;;              :onChange
            ;;              #(update-mines! (js/parseInt
            ;;                               (.. % -target -value)))}))
            (dom/button {:onClick #(reset-grid! (:width @state)
                                                (:height @state)
                                                (:mines @state))} "Reset"))
   (Grid {:state (:grid @state)})))

(defn ^:export start! [node]
  (react-dom/render (Container) node))
