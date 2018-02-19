(ns lilactown.state
  (:require [lilactown.data :as data]
            [lilactown.config :as config]
            [mount.core :refer [defstate]]
            [tick.core]
            [tick.timeline]
            [tick.clock]
            [tick.schedule]))

;; The request to the medium API is very slow
;; sometimes taking 1-6 seconds!!
;; So we'll cache it in memory and refresh
;; occasionally

(defstate medium
  :start (agent (data/medium)))

(defstate github
  :start (agent (data/github (get-in config/env [:secrets :github]))))


(defn update-medium []
  (send-off medium #(do % (data/medium))))

(defn update-github []
  (send-off github #(do % (data/github (get-in config/env [:secrets :github])))))

(defn poll-timeline []
  (tick.timeline/timeline
   (tick.timeline/periodic-seq (tick.clock/now)
                               (tick.core/hours 6))))

(defn schedule-poll [timeline]
  (tick.schedule/schedule #(do (update-medium)
                               (update-github)
                               (println %))
                          timeline))

(defn start-polling [schedule]
  (tick.schedule/start schedule (tick.clock/clock-ticking-in-seconds)))

(defstate data-polling
  :start (let [scheduler (-> (poll-timeline)
                             (schedule-poll))]
           (start-polling scheduler)
           scheduler)
  :stop (tick.schedule/stop data-polling))
