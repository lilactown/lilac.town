(ns lilactown.site.core
  (:require [mount.core :as mount :refer [defstate]]
            [ring.adapter.jetty :as j]
            [ring.middleware.resource :refer [wrap-resource]]
            [ring.middleware.content-type :refer [wrap-content-type]]
            [ring.middleware.params :refer [wrap-params]]
            [hiccup.core :refer [html]]
            [lilactown.site.home :as home]
            [lilactown.site.home.state :as state]
            [lilactown.site.home.data :as data])
  (:gen-class))

(defn home [request]
  {:status 200
   :headers {"Content-Type" "text/html"}
   :body (html (home/render {:github @state/github
                             :medium @state/medium}))})

(def app (-> home
             (wrap-params)
             (wrap-resource "public")
             (wrap-content-type)))

(defn start [{:keys [port]}]
  (j/run-jetty #'app {:port (or port 3000) :join? false}))

(defstate server
  :start (->> (mount/args)
              (start))
  :stop (.stop server))
