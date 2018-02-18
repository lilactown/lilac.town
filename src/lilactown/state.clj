(ns lilactown.state
  (:require [lilactown.data :as data]))

;; The request to the medium API is very slow
;; sometimes taking 1-6 seconds!!
;; So we'll cache it in memory and refresh
;; occasionally

(def medium (agent (data/medium)))

(def github (agent (data/github)))
