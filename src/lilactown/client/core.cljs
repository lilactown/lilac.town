(ns lilactown.client.core
  (:require [shadow.loader :as loader]
            [cljs.reader]
            [clojure.string :as s]
            [lilactown.dom :include-macros true]))

(defn- ns-var->js [var]
  (s/replace (munge var) "_SLASH_" "."))

(defn -load [node]
  (let [module (.getAttribute node "data-module")
        init (.getAttribute node "data-init")
        dom-ref (.getAttribute node "data-ref")
        data (.-innerHTML node)]
    (.then (loader/load module)
           (fn []
             (println [module] "load")
             (let [init-fn (goog/getObjectByName (ns-var->js init))]
               (js/console.log (ns-var->js init) init-fn)
               (when init-fn
                 (init-fn (case dom-ref
                            ":self" node
                            ":parent" (.-parentElement node)
                            (.querySelector js/document dom-ref))
                          (cljs.reader/read-string data))))))))

(defn load! []
  (let [nodes (. js/document querySelectorAll
                 "script[type=\"lilactown/module\"]")]
    (doseq [node (array-seq nodes)]
      (-load node))))

(defn stop! []
  (js/console.log "Stopped"))

(defn ^{:export true
        :dev/after-load true}
  start! []
  (load!)
  (js/console.log "Started"))

(start!)
