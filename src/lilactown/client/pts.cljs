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
                     (.setup #js {;; :bgcolor "#fff"
                                  :resize true
                                  :retina true}))
           form (.getForm space)]
       (r/set-this! :space space)
       (r/set-this! :form form)

       (.add ^js space
             (fn [time, ftime]
               (let [radius (* 20 (.cycle pts/Num (-> time
                                                      (mod 1000)
                                                      (/ 1000))))]
                 (-> form
                     (.fill "#09f")
                     (.point ;; (.-pointer ^js space)
                      (pt {:x 140 :y 80})
                      radius
                      "circle"))
                 )))

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
    (dom/div
     (dom/canvas {:ref #(r/set-this! :pts-canvas %)}))))

(defn ^:export start! [node]
  (react-dom/render (Pts) node))
