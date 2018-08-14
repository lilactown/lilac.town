(ns lilactown.css
  (:require [emotion :as emotion]))

(defn edn [& styles]
  (apply emotion/css (clj->js styles)))
