(ns lilactown.workspace.pts
  (:require [nubank.workspaces.core :as ws]
            [nubank.workspaces.card-types.react :as ct.react]
            [lilactown.client.pts :as art]
            ["pts" :as pts]))

(defn draw [space form]
  (.add ^js space
        (fn [time, ftime]
          (println (-> time (mod 100) (/ 100)))
          (let [radius (* 20
                          (.cycle pts/Num (-> time
                                              (mod 1000)
                                              (/ 1000))))]
            (-> form
                (.fill "pink")
                (.point (art/pt {:x 140 :y 80})
                        radius
                        "circle"))))))

(ws/defcard Pts-test
  (ct.react/react-card
   #_"Hello"
   (art/Pts
    {:style {:height "200px"}
     :setup {:bgcolor "#1b1b1b"}}
    #'draw)))
