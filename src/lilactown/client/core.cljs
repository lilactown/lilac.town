(ns lilactown.client.core
  (:require [react :as react]
            [react-dom :as react-dom]))

(defn factory [component]
  (let [f (react/createFactory component)]
    (fn [props children]
      (if (map? props)
        (f (clj->js props) children)

        ;; props are children
        (f nil props)))))

(defn ^:dev/before-load stop! []
  (js/console.log "Stopped"))

(defn ^:dev/after-load start! []
  (js/console.log "Started"))

(def div (factory "div"))

(def h1 (factory "h1"))


(react-dom/render (h1 {:className "title"} "will.acton") (. js/document getElementById "title"))

(start!)
