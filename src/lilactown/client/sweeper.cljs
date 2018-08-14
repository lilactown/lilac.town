(ns lilactown.client.sweeper
  (:require [lilactown.react :as r]
            [lilactown.react.dom :as dom]))

(defn square-state [size n s]
  (assoc s
         :row (inc (quot n size))
         :col (inc (mod n size))))

(defn initial-state [size mines]
  ;; (shuffle
  ;;  (for [row (range 1 size)
  ;;        col (range 1 size)]
  ;;    {:explodes? (<= (* row col) mines)}))
  (-> (- (* size size) mines)
      (repeat {:explodes? false})
      (into (repeat mines {:explodes? true}))
      (shuffle)
      (as-> squares
          (map-indexed (partial square-state size) squares))))

(r/defnc Cell [{:keys [col row explodes?]}]
  (dom/div {:style #js {:gridColumn col
                        :gridRow row
                        :width "30px"
                        :height "30px"
                        :backgroundColor (if explodes?
                                           "black"
                                           "rgb(187, 164, 255)")
                        :color "white"}}))

(r/defnc Container []
  (dom/div
   (dom/div {:style #js {:display "grid"
                         :gridGap "5px"
                         ;; :gridTemplateColumns "repeat(10, 1fr)"
                         }}
            (for [{:keys [col row explodes?]} (initial-state 10 5)]
              (Cell {:key [col row] :row row :col col
                     :explodes? explodes?})))))
