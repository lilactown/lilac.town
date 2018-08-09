(ns lilactown.client.pts
  (:require ;; [lilactown.react :as r]
            ;; [lilactown.react.dom :as dom]
            ;; [react-dom :as react-dom]
            ;; ["pts" :as pts]
            ))

;; (r/defcomponent Pts
;;   :create-chart
;;   (r/send-this
;;    (fn [this]
;;      ;; setup
;;      (let [space (-> (r/this :pts-canvas)
;;                      (pts/CanvasSpace.)
;;                      (.setup #js {:bgColor "#fff"
;;                                   :resize true
;;                                   :retina true}))
;;            form (.getForm space)]
;;        (r/set-this! :space space)
;;        (r/set-this! :form form)

;;        (.add ^js space
;;              (fn []
;;                (.point ^js form
;;                        (.-pointer ^js space) 10)))

;;        (-> space
;;            (.bindMouse)
;;            (.bindTouch))

;;        (-> space
;;            (.playOnce 200)))))

;;   :componentDidMount
;;   (fn [this]
;;     ((r/this :create-chart)))

;;   :componentDidUpdate
;;   (fn [this]
;;     (js/console.log "updated")
;;     ((r/this :space :playOnce) 0))

;;   :render
;;   (fn [this]
;;     (dom/div
;;      (dom/canvas {:ref #(r/set-this! :pts-canvas %)}))))

(def pts (.-Pts js/window))

;; set the namespace
(.namespace pts js/window)

(def CanvasSpace (.-CanvasSpace pts))

(def canvas (let [-canvas (.createElement js/document "canvas")]
              (set! (.-id -canvas) "pts-canvas")
              -canvas))

(defn render []
  (let [space (-> (CanvasSpace. "#pts-canvas")
                  (.setup #js {:retina true
                               :resize true}))
        form (.getForm ^js space)]
    ;; animation
    (.add space #(do
                   (js/console.log "doin'")
                   (.point form (.-pointer ^js space) 10)))

    ;;start
    (-> ^js space
        (.playOnce 0)
        (.bindMouse)
        (.bindTouch))))

(defn ^:export start! [node]
  (set! (.-innerHTML node) "")
  (.appendChild node canvas)
  (render))
