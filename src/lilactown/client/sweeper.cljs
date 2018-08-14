(ns lilactown.client.sweeper
  (:require [lilactown.react :as r]
            [lilactown.react.dom :as dom]))

(defn initial-square [size n s]
  (assoc s
         :row (inc (quot n size))
         :col (inc (mod n size))
         :marked? false))

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
          (map-indexed (partial initial-square size) squares))))

(r/defnc Square [{:keys [col row explodes? marked?]}]
  (dom/div {:style #js {:gridColumn col
                        :gridRow row
                        :width "30px"
                        :height "30px"
                        :backgroundColor (if explodes?
                                           "black"
                                           "rgb(187, 164, 255)")
                        :color "white"
                        :fontFamily "sans-serif"
                        :display "flex"
                        :justifyContent "center"
                        :alignItems "center"}}
           (when marked? "?")))

(r/defnc Container []
  (dom/div
   (dom/div {:style #js {:display "grid"
                         :gridGap "5px"
                         ;; :gridTemplateColumns "repeat(10, 1fr)"
                         }}
            (for [square (initial-state 10 10)]
              (Square square)))))
