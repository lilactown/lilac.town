(ns lilactown.client.core
  (:require [lilactown.client.title :as title]))

(defn stop! []
  (js/console.log "Stopped"))

(defn ^{:export true
        :dev/after-load true}
  start! []
  (title/start! (.getElementById js/document "title"))
  (js/console.log "Started"))

(start!)
