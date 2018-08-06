(ns lilactown.react)

(defmacro child-fn
  "A helper macro for using function-as-children in React. Pass in expected
  arguments and a body to return, and it will automatically convert the args
  to CLJS (with keywordize keys) and destructure them for you."
  [props & body]
  `(fn [args#]
     (let [~@props (~'js->clj args# :keywordize-keys true)]
       ~@body)))

(defmacro send-this
  "A helper macro for defining methods on a React component definition.
  Creates a function that receives the component as it's first argument
  and ~args~ as the following arguments."
  [args f]
  (let [this (gensym 'this)]
    `(fn ~args
       (~'this-as ~this
        (~f ~this ~@args)))))

(defmacro this
  "A helper macro for obtaining a property or method defined on a component
  while inside a method on the component."
  [& syms]
  `(get-in$ ~'this ~@(map name syms)))

(defmacro props
  "A helper macro for obtaining a value on the props object defined by the path
  of keys passed in while in a method on the component."
  [& keys]
  `(props* ~'this ~@keys))

(defmacro children
  "A helper macro for obtaining the \"children\" prop while in a method on the
  component."
  []
  `(children* ~'this))

(defmacro set-this!
  "Sets a property `k` on the component to value `v`. Takes the string name
  of `k`."
  [k v]
  `(set$ ~'this (name ~k) ~v))

(defmacro set-state!
  "Helper macro for setting the state of a component while inside a method on
  the component. Takes a function `f` with the signature
  (#js state, #js props) => #js update."
  [f]
  `(.setState ~'this
      ~f))