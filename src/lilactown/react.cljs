(ns lilactown.react
  (:require [react :as react]
            [goog.object :as gobj]
            [taoensso.timbre :as t :include-macros true]
            [create-react-class :as create-react-class])
  (:require-macros [lilactown.react]))

(defn factory
  "Takes a React component, and creates a function that returns
  a new React element"
  [component]
  (let [f (react/createFactory component)]
    (fn [props & children]
      (if (map? props)
        (apply f (clj->js props) children)

        ;; props are children
        (apply f nil props children)))))

(defn- assoc-when
  "Assocs m with k v, only when pred is true"
  [m pred k v]
  (if pred
    (assoc m k v)
    m))

(defn- bind-method
  "Creates "
  [m k]
  (assoc-when
   m
   (m k)
   k (lilactown.react/send-this [] (m k))))

(def get-in$ gobj/getValueByKeys)

(def set$ gobj/set)

(defn props*
  "Takes a component and a variable number of string keys, and returns the value
  in the props object on the component at the end of the path."
  [this & keys]
  (apply get-in$ this "props" keys))

(defn children*
  "Takes a component and returns the \"children\" prop"
  [this]
  (props* this "children"))

(defn state*
  "Takes a component and a variable number of string keys, and returns the value
  in the state object on the component at the end of the path."
  [this & keys]
  (apply get-in$ "state" keys))

(defn component
  "Creates a new component factory from a given React component definition."
  [definition]
  (-> definition
      (bind-method :getInitialState)
      (bind-method :componentWillMount)
      (bind-method :componentDidMount)
      (bind-method :componentWillUnmount)
      (bind-method :render)
      (clj->js)
      (create-react-class)
      (factory)))

(defn reactive-component
  "Creates a new ReactiveComponent factory from a given React component
  definition. Special methods/properties are:

   - :watch - a fn that returns a map of key-values where values are the atoms
              to watch and reactively re-render

   - :init - a function called before the watches are added to the atoms.
             Gets passed in the uuid generated for the element and a reference
             to the component.

   - :should-update - a function that is passed in the element uuid, old value
                      of the atom and new value of the atom - returns a boolean
                      discerning whether the component should update or not."
  [{:keys [watch init should-update
           display-name]
    :or {should-update (fn [_ _ _ _] true)}
    :as definition}]
  (-> {:displayName (or display-name "ReactiveComponent")

       :componentWillMount
       (fn [this]
         (let [id (random-uuid)]
           (lilactown.react/set-this! :watch-id id)
           (t/debug "[reactive]" "Initializing" id)
           (when init
             (t/debug "[reactive]" "Mounting" id)
             (init id this))))

       :componentDidMount
       (fn [this]
         (let [id (lilactown.react/this :watch-id)]
           (when watch
             (doseq [[k w] (watch this)]
               (add-watch
                w
                id
                (fn [_k _r old-v new-v]
                  (when (should-update k old-v new-v id)
                    (. this forceUpdate))))))))

       :componentWillUnmount
       (fn [this]
         (t/debug "[reactive]" "Unmounting" (lilactown.react/this :watch-id))
         (when watch
           (doseq [[k w] (watch this)]
             (remove-watch w (lilactown.react/this :watch-id)))))}
      (merge definition)
      (merge {:render
              (fn [this]
                (t/debug "[reactive]" "Rendering" (lilactown.react/this :watch-id))
                ((:render definition) this
                 ;; deref all the atoms in the watch map
                 (when watch
                   (reduce-kv #(assoc %1 %2 @%3) {} (watch this)))))})
      (component)))

