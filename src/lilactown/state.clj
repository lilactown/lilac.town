(ns lilactown.state
  (:require [lilactown.data :as data]
            [lilactown.config :as config]
            [mount.core :refer [defstate]]))

;; The request to the medium API is very slow
;; sometimes taking 1-6 seconds!!
;; So we'll cache it in memory and refresh
;; occasionally

(defstate medium
  :start (agent (data/medium)))

(defstate github
  :start (agent (data/github (get-in config/env [:secrets :github]))))
