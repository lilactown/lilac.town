(ns user
  (:require [shadow.cljs.devtools.server :as server]
            [shadow.cljs.devtools.api :as shadow]
            [lilactown.core :as lilactown]
            [mount.core :as mount]))

(defn start! []
  (mount/start)
  (server/start!)
  (shadow/watch :client))

(defn stop! []
  (server/stop!)
  (mount/stop))
