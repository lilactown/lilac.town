(ns lilactown.react)

(defmacro send-this
  "A helper macro for defining methods on a React component definition.
  Creates a function that receives the component as it's first argument
  and applies any other args as the following arguments."
  [f]
  (let [this (gensym 'this)]
    `(fn [& args#]
       (~'this-as ~this
        (apply ~f ~this args#)))))

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

(defmacro fnc [props & body]
  (if (> (count props) 0)
    `(fn [this#]
       (let [~@props (lilactown.react/shallow-js->clj
                      (get-in$ this# "props"))]
         ~@body))

    `(fn []
       ~@body)))

(defmacro defnc
  "Defines a simple \"functional\" React component factory that takes in props as
  its argument and returns a React element. Props are shallowly converted to a
  CLJ map and can be destructured just like in defn."
  [name props & body]
  `(def ~name
     (lilactown.react/pure-component
      {:displayName ~(str name)
       :render
       (fnc ~props ~@body)})))

(defmacro defcomponent
  "Defines a React component class factory. `definition` is a spread of
  key-value pairs, where keys are keywords that will be used as method names,
  and values are functions. Methods are automatically bound to the component
  class, and standard React methods automatically are passed in `this` as the
  first argument to them."
  [name & {:keys [render] :as definition}]
  `(def ~name
     (lilactown.react/component
      (merge {:displayName ~(str name)}
             ~definition))))

(defmacro defpure
  "Defines a React component class factory that implements
  `shouldComponentUpdate` as a shallow equality check. Good for use with
  immutable data structures."
  [name & {:keys [render] :as definition}]
  `(def ~name
     (lilactown.react/pure-component
      (merge {:displayName ~(str name)}
             ~definition))))

(defmacro defreactive
  "Defines a new ReactiveComponent factory from a given React component
  definition. Special methods/properties are:

   - :watch - a fn that returns a map of key-values where values are the atoms
              to watch and reactively re-render

   - :init - a function called before the watches are added to the atoms.
             Gets passed in the uuid generated for the element and a reference
             to the component.

   - :should-update - a function that is passed in the element uuid, old value
                      of the atom and new value of the atom - returns a boolean
                      discerning whether the component should update or not."
  [name & {:keys [displayName watch init should-update
                  render] :as definition}]
  `(def ~name
     (lilactown.react/reactive-component
      (merge {:displayName ~(str name)}
             ~definition))))
