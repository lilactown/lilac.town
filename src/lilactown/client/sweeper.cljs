(ns lilactown.client.sweeper
  (:require [lilactown.react :as r]
            [lilactown.react.dom :as dom]
            [lilactown.css :as css]))

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
                         :marked? false)]))))

(def square-style
  (fn [explodes?]
    (css/edn
     {:width "30px"
      :height "30px"
      :background-color (if explodes?
                          "black"
                          "rgb(187, 164, 255)")
      :color "white"
      :font-family "sans-serif"
      :display "flex"
      :justify-content "center"
      :align-items "center"
      :box-shadow "2px 2px 3px rgba(100, 100, 100, .5)"})))

(r/defnc Square [{:keys [col row explodes? marked?]}]
  (dom/div {:style #js {:gridColumn col
                        :gridRow row}
            :className (square-style explodes?)}
           (when marked? "?")))

(r/defnc Grid [{state :state}]
  (dom/div
   (dom/div {:style #js {:display "grid"
                         :gridGap "5px"}}
            ;; keys are [col row], value is square state
            (for [[[col row] square] state]
              (Square (assoc square
                             :col col
                             :row row))))))

(r/defnc Container []
  (Grid {:state (initial-state 15 80)}))
