(ns lilactown.client.title.core)

(println "hi")

(defonce canvas (let [canvas- (.createElement js/document "canvas")]
                    (set! (.-width canvas-) "300")
                    (set! (.-height canvas-) "200")
                    canvas-))

(defn init []
  (js/console.log "initing")
  (let [title-container (.querySelector js/document ".title")]
    (.appendChild title-container canvas)))

(defn start []
  (js/console.log "starting"))

(defn stop []
  (js/console.log "stopping"))
