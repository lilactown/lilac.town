(ns lilactown.site.core
  (:require [mount.core :as mount :refer [defstate]]
            [ring.adapter.jetty :as j]
            [ring.middleware.content-type :refer [wrap-content-type]]
            [ring.middleware.params :refer [wrap-params]]
            [reitit.ring :as ring]
            [hiccup.core :refer [html]]
            [lilactown.site.home :as home]
            [lilactown.site.home.state :as state]
            [lilactown.site.home.data :as data]
            [lilactown.site.games.sweeper :as sweeper]
            [lilactown.site.projects.visual-spec :as visual-spec]
            [lilactown.site.writing :as writing]))

(defn home [request]
  {:status 200
   :headers {"Content-Type" "text/html"}
   :body (html (home/render {:github @state/github
                             :medium @state/medium
                             :version state/version}))})

(defn version [request]
  {:status 200
   :headers {"Content-Type" "text/plain"}
   :body state/version})

(defn sweeper [request]
  {:status 200
   :headers {"Content-Type" "text/html"}
   :body (html (sweeper/render))})

(defn visual-spec [request]
  {:status 200
   :headers {"Content-Type" "text/html"}
   :body (html (visual-spec/render))})

(defn writing-home [request]
  {:status 200
   :headers {"Content-Type" "text/html"}
   :body (html (writing/render))})

(defn writing-post [{:keys [path-params]}]
  {:status 200
   :headers {"Content-Type" "text/html"}
   :body (html (writing/render-post (:slug path-params)))})

(def app (-> (ring/ring-handler
              (ring/router
               ["/"
                ["" {:get {:handler home}}]
                ["version" {:get {:handler version}}]
                ["games" ["/sweeper" {:get {:handler sweeper}}]]
                ["projects" ["/visual-spec" {:get {:handler visual-spec}}]]
                ["writing" {:get {:handler writing}}]
                ["writing/:slug" {:get {:handler writing-post}}]]
               {:data {:middleware [wrap-params
                                    wrap-content-type]}})
              (ring/routes
               (ring/create-resource-handler {:path "/" :root "/public"})
               (ring/create-default-handler)))))

(defn start [{:keys [port]}]
  (j/run-jetty #'app {:port (or port 3000) :join? false}))

(defstate server
  :start (->> (mount/args)
              (start))
  :stop (.stop server))

#_(mount/stop)
#_(mount/start)
