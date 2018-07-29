(ns lilactown.dom)

(defmacro child-fn [props & body]
  `(fn [args#]
     (let [~@props (~'js->clj args# :keywordize-keys true)]
       ~@body)))

(defmacro send-this [args f]
  (let [this (gensym 'this)]
    `(fn ~args
       (~'this-as ~this
        ~(when (count args)
           `(apply ~f ~this ~@args)
           `(~f ~this))))))

(defmacro this [& syms]
  `(get-in$ ~'this ~@(map name syms)))

(defmacro props [& keys]
  `(props* ~'this ~@keys))

(defmacro children []
  `(children* ~'this))

(defmacro set-this! [k v]
  `(set$ ~'this (name ~k) ~v))

(defmacro set-state! [f]
  `(.setState ~'this
      ~f))
