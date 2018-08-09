(ns lilactown.client.pts
  (:require [lilactown.react :as r]
            [lilactown.react.dom :as dom]
            [react-dom :as react-dom]
            ["pts" :as pts]))

(r/defcomponent Pts
  :create-chart
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
                     (.point (.-pointer ^js space)
                             radius
                             "circle")))))

       (-> space
           (.bindMouse)
           (.bindTouch))

       (-> space
           (.play)))))

  :destroy-chart
  (r/send-this
   (fn [this]
     (-> (r/this :space)
         (.stop)
         (.removeAll))))

  :componentDidMount
  (fn [this]
    ((r/this :create-chart)))

  :componentWillUnmount
  (fn [this]
    ((r/this :destroy-chart)))

  ;; :componentDidUpdate
  ;; (fn [this]
  ;;   (js/console.log "updated")
  ;;   ((r/this :space :playOnce) 0))

  :render
  (fn [this]
    (dom/div
     (dom/canvas {:ref #(r/set-this! :pts-canvas %)}))))

(defn ^:export start! [node]
  (react-dom/render (Pts) node))
