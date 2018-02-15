(ns lilactown.core
  (:require [mount.core :as mount :refer [defstate]]
            [ring.adapter.jetty :as j]
            [ring.middleware.resource :refer [wrap-resource]]
            [ring.middleware.content-type :refer [wrap-content-type]]
            [hiccup.core :refer [html]]
            [lilactown.pages.home :as home]))

(defn handler [request]
  {:status 200
   :headers {"Content-Type" "text/html"}
   :body (html (home/html))})

(def app (-> handler
             (wrap-resource "public")
             (wrap-content-type)))

(defstate server
  :start (j/run-jetty #'app {:port 3000 :join? false})
  :stop (.stop server))
