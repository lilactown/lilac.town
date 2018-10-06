(ns lilactown.client.pts
  (:require [lilactown.react :as r]
            [lilactown.react.dom :as dom]
            [react-dom :as react-dom]
            ["pts" :as pts]))

(defn pt
  ([] (pts/Pt.))
  ([obj] (pts/Pt. (clj->js obj)))
  ([& args] (apply pts/Pt. args)))

(r/defcomponent Pts
  :create-pts
  (r/send-this
   (fn [this]
     ;; setup
     (let [space (-> (r/this :pts-canvas)
                     (pts/CanvasSpace.)
                     (.setup (or (clj->js (r/props :setup)) #js {})))
           form (.getForm space)]
       (r/set-this! :space space)
       (r/set-this! :form form)

       ((or (r/props :children) identity) space form)

       (-> space
           (.bindMouse)
           (.bindTouch))

       (-> space
           (.play)))))

  :destroy-pts
  (r/send-this
   (fn [this]
     (-> ^js (r/this :space)
         ^js (.stop)
         (.removeAll))))

  :componentDidMount
  (fn [this]
    ((r/this :create-pts)))

  :componentWillUnmount
  (fn [this]
    ((r/this :destroy-pts)))

  :render
  (fn [this]
    (let [height (r/props :height)
          width (r/props :width)]
    (dom/div
     (dom/canvas {:style (clj->js (r/props :style))
                  :ref #(r/set-this! :pts-canvas %)})))))

(defn ^:export start! [node]
  (react-dom/render (Pts) node))
