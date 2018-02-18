(ns lilactown.core
  (:require [mount.core :as mount :refer [defstate]]
            [ring.adapter.jetty :as j]
            [ring.middleware.resource :refer [wrap-resource]]
            [ring.middleware.content-type :refer [wrap-content-type]]
            [ring.middleware.params :refer [wrap-params]]
            [hiccup.core :refer [html]]
            [lilactown.pages.home :as home]
            [lilactown.state :as state]
            [lilactown.data :as data])
  (:gen-class))

(defn handler [request]
  {:status 200
   :headers {"Content-Type" "text/html"}
   :body (html (home/html {:github @state/github
                           :medium @state/medium}))})

(def app (-> handler
             (wrap-params)
             (wrap-resource "public")
             (wrap-content-type)))

(defn parse-args [[port]]
  {:port (Integer. port)})

(defn start [{:keys [port]}]
  (j/run-jetty #'app {:port (or port 3000) :join? false}))

(defstate server
  :start (->> (mount/args)
              (start))
  :stop (.stop server))

(comment (mount/start))

(defn -main [& args]
  (println args)
  (->> args
       (parse-args)
       (mount/start-with-args)))
