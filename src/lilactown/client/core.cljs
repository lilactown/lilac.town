(ns lilactown.client.core
  (:require [shadow.loader :as loader]
            [cljs.reader]
            [clojure.string :as s]
            [taoensso.timbre :as t]
            [lilactown.dom :include-macros true]))

(defonce loaded (atom {}))

(defn- ns-var->js [var]
  (s/replace (munge var) "_SLASH_" "."))

(defn -run-init [{:keys [init dom-ref data script-node]
                  :or {script-node nil}}]
  (let [init-fn (goog/getObjectByName (ns-var->js init))]
    (when init-fn
      (init-fn (case dom-ref
                 ":self" script-node
                 ":parent" (.-parentElement script-node)
                 (.querySelector js/document dom-ref))
               (cljs.reader/read-string data)))))

(defn -load [module {:keys [init dom-ref data script-node] :as module-def}]
  (.then (loader/load module)
         (fn []
           (t/info [module] "Loaded")
              (-run-init module-def))))

(defn load! []
  (let [nodes (. js/document querySelectorAll
                 "script[type=\"lilactown/module\"]")]
    (doseq [node (array-seq nodes)]
      (let [module (.getAttribute node "data-module")
            load-def {:init (.getAttribute node "data-init")
                      :dom-ref (.getAttribute node "data-ref")
                      :data (.-innerHTML node)
                      :script-node node}]
        (if (and (loader/loaded? module)
                 (@loaded module))
          (do (t/info [module] "Already loaded")
              (-run-init (@loaded module)))
          (do (t/info [module] "Loading...")
              (swap! loaded assoc
                     module load-def)
              (-load module load-def)))))))

(defn stop! []
  (t/info "Stopped"))

(defn ^{:export true
        :dev/after-load true}
  start! []
  (load!)
  (t/info "Started"))

(start!)
