(ns lilactown.dom)

(defmacro child-fn [props & body]
  `(fn [args#]
     (let [~@props (~'js->clj args# :keywordize-keys true)]
       ~@body)))
