(ns lilactown.workspace.pts
  (:require [nubank.workspaces.core :as ws]
            [nubank.workspaces.card-types.react :as ct.react]
            [lilactown.client.pts :as art]
            ["pts" :as pts]))

(defn draw [space form]
  (.add ^js space
        (fn [time, ftime]
          (let [radius (* 20 (.cycle pts/Num (-> time
                                                 (mod 1000)
                                                 (/ 1000))))]
            (-> form
                (.fill "#09f")
                (.point (art/pt {:x 140 :y 80})
                        radius
                        "circle"))
            ))))

(ws/defcard Pts-test
  (ct.react/react-card
   (art/Pts
    {:style {:height "200px"}}
    draw)))
