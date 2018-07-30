(ns lilactown.core
  (:require [lilactown.site.core :as site]
            [mount.core :as mount]))

(comment (mount/start))

(defn parse-args [[port]]
  {:port (Integer. port)})

(defn -main [& args]
  (println args)
  (->> args
       (parse-args)
       (mount/start-with-args)))
