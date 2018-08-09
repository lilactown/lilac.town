(ns lilactown.workspace.pts
  (:require [nubank.workspaces.core :as ws]
            [nubank.workspaces.card-types.react :as ct.react]
            [lilactown.react :as r]
            [lilactown.react.dom :as dom]
            ["pts" :as pts]))


(defn render [space form]
  (println "rendering!")
  (.add space
        (fn []
          (.point form
                  (.-pointer space) 10))))

(r/defcomponent Pts
  :create-chart
  (r/send-this
   (fn [this]
     ;; setup
     (let [space (-> (r/this :pts-canvas)
                     (pts/CanvasSpace.)
                     (.setup #js {:bgColor (or (r/props :bg-color) "#6cf")
                                  :resize true
                                  :retina true}))
           form (.getForm space)]
       (r/set-this! :space space)
       (r/set-this! :form form)
       (render space form))))

  :componentDidMount
  (fn [this]
    ((r/this :create-chart)))

  :render
  (fn [this]
    (dom/div
     {:className "pts-chart"}
     (dom/canvas {:ref #(r/set-this! :pts-canvas %)}))))

(ws/defcard Pts-test
  (ct.react/react-card
   (Pts)))
