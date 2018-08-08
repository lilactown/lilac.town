(ns user
  (:require [lilactown.core :as lilactown]
            [mount.core :as mount]))

(defn start! []
  (mount/start))

(defn stop! []
  (mount/stop))
