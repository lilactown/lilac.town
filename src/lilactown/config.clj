(ns lilactown.config
  (:require [mount.core :refer [defstate]]
            [clojure.java.io :as io]
            [clojure.edn :as edn]))

(defstate env
  :start {:secrets (->> (io/resource "secrets.edn")
                        (slurp)
                        (edn/read-string))})
