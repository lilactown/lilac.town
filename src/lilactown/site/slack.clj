(ns lilactown.site.slack
  (:require [ring.util.response :as res]
            [ring.util.request :as req]
            [muuntaja.core :as m]))

(def !logs (atom []))

(defmulti slack-event :type)

(defmethod slack-event "url_verification"
  [body]
  (-> (:challenge body)
      (res/response)))

(defn main [request]
  (let [body (:body-params request)]
    (swap! !logs conj body)
    (slack-event body)))
