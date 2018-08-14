(ns lilactown.css
  (:require [emotion :as emotion]))

(defn edn [& styles]
  (apply emotion/css (clj->js styles)))

(defn keyframes [& styles]
  (apply emotion/keyframes (clj->js styles)))
