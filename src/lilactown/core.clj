(ns lilactown.core
  (:require [lilactown.site.core :as site]
            [mount.core :as mount])
  (:gen-class))

(comment (mount/start))

(defn parse-args [[port]]
  {:port (Integer. port)})

(defn -main [& args]
  (println args)
  (->> args
       (parse-args)
       (mount/start-with-args)))
