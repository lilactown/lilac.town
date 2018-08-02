(ns lilactown.core
  (:require [lilactown.site.core :as site]
            [mount.core :as mount])
  (:gen-class))

(comment (mount/start))

(defn parse-args [[port version]]
  {:port (Integer. port)
   :version version})

(defn -main [& args]
  (println args)
  (->> args
       (parse-args)
       (mount/start-with-args)))
