(ns lilactown.core
  (:require [mount.core :as mount :refer [defstate]]
            [ring.adapter.jetty :as j]
            [ring.middleware.resource :refer [wrap-resource]]
            [ring.middleware.content-type :refer [wrap-content-type]]
            [ring.middleware.params :refer [wrap-params]]
            [hiccup.core :refer [html]]
            [lilactown.pages.home :as home])
  (:gen-class))

(defn handler [request]
 (let [bgcolor (get-in request [:params "bg"])] 
   {:status 200
    :headers {"Content-Type" "text/html"}
    :body (html (home/html bgcolor))}))

(def app (-> handler
             (wrap-params)
             (wrap-resource "public")
             (wrap-content-type)))

(defstate server
  :start (j/run-jetty #'app {:port 3000 :join? false})
  :stop (.stop server))

(defn -main [& args]
  (mount/start))
