(ns lilactown.client.title.core)

(println "hi")

(defonce init? (atom true))

(defonce canvas (let [canvas- (.createElement js/document "canvas")]
                    (set! (.-width canvas-) "300")
                    (set! (.-height canvas-) "43")
                    (set! (.-style canvas-) "vertical-align: text-bottom")
                    canvas-))

(defonce context (.getContext canvas "2d" (clj->js {:alpha false})))

(defn clear []
  (.clearRect context 0 0 (.-width canvas) (.-height canvas)))

(defn rect! [ctx {:keys [x y width height fill stroke]}]
  (when fill
    (let [old-style (.-fillStyle ctx)]
      (set! (.-fillStyle ctx) fill)
      (.fillRect ctx x y width height)
      (set! (.-fillStyle ctx) old-style)
      ctx))
  (when stroke
    (let [old-style (.-strokeStyle ctx)]
      (js/console.log old-style stroke)
      (set! (.-strokeStyle ctx) stroke)
      (.strokeRect ctx x y width height)
      (set! (.-strokeStyle ctx) old-style)
      ctx)))

(defn text! [ctx {:keys [text x y max-width font fill]}]
  (let [old-font (.-font ctx)
        old-style (.-fillStyle ctx)]
    (set! (.-font ctx) font)
    (set! (.-fillStyle ctx) fill)
    (.fillText ctx text x y (or max-width js/undefined))
    (set! (.-font ctx) old-font)
    (set! (.-fillStyle ctx) old-style)
    ctx))

(def text-style {:fill "#3b3b3b"
                 :font "1em Roboto Slab"})

(def bottom (- (.-height canvas) 9))

(def initial-text (merge {:text "lilac.town"
                          :x 0
                          :y bottom} text-style ;; {:fill "red"}
                         ))

(def initial-letters (map #(merge % text-style {:y bottom})
                          [{:text "l" :x 0}
                           {:text "i" :x 10}
                           {:text "l" :x 20.5}
                           {:text "a" :x 30.8}
                           {:text "c" :x 48.3}
                           {:text "." :x 65.5}
                           {:text "t" :x 73.2}
                           {:text "o" :x 84.7}
                           {:text "w" :x 102.5}
                           {:text "n" :x 129}]))

(def _ identity)

(def frames [[{:y 10} _ _ _ _ _ _ _ _ _]])

(defn apply-frame [initial frame]
  (->> (map vector frame initial)
        (map (fn [[f s]]
              (if (map? f)
                (merge-with + f s)
                (f s))))
        ))

(defn draw-frame! [frame]
  (dorun (map #(text! context %) frame)))

(defn draw! []
  (js/console.log "drawing")
  (rect! context {:x 0 :y 0 :width (.-width canvas) :height (.-height canvas)
                  :fill "#DCD0FF"})
  (draw-frame! initial-letters)
  (draw-frame! (apply-frame initial-letters (first frames)))
  )

(defn ^:export init []
  (js/console.log "initing")
  (reset! init? false)
  (let [title-container (.querySelector js/document ".title")]
    (set! (.-innerHTML title-container) "")
    (.appendChild title-container canvas))
  (draw!))

(defn start []
  (js/console.log "starting")
  (draw!)
  )

(defn stop []
  (js/console.log "stopping")
  (clear))
