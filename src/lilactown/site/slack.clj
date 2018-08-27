(ns lilactown.site.slack
  (:require [ring.util.response :as res]
            [ring.util.request :as req]))

(def !logs (atom []))

(def !messages (atom []))

(defmulti slack-event :type)

(defmethod slack-event "url_verification"
  [body]
  (swap! !messages conj [:slack/verification {:token (:token body)
                                          :challenge (:challenge body)}])
  (-> (:challenge body)
      (res/response)))

(defmethod slack-event "message"
  [event]
  (swap! !messages conj [:slack/message {:channel (:channel event)
                                     :user (:user event)
                                     :text (:text event)
                                     :time (:ts event)}])
  (res/response "OK"))

(defn api [request]
  (let [body (:body-params request)]
    (swap! !logs conj body)
    (if (:event body)
      (slack-event (:event body))
      (slack-event body))))

(defn logs [request]
  (let [filters (:query-params request)
        filter-fn identity]
    (-> @!logs
        (res/response))))
