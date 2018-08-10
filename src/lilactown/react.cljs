(ns lilactown.react
  (:require [react :as react]
            [goog.object :as gobj]
            [taoensso.timbre :as t :include-macros true]
            [create-react-class :as create-react-class])
  (:require-macros [lilactown.react]))

;; Utils

(defn shallow-js->clj
  "Convert a Javascript object into a Clojure map *shallowly*. See
   `shallow-clj->js`."
  [o]
  (let [kseq (gobj/getKeys o)]
    (into {} (map (fn [k] [(keyword k) (aget o k)]) kseq))))

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

(defn- ?assoc
  "Assocs m with k v only when k doesn't already exist"
  [m k v]
  (assoc-when m (comp not k) k v))

(defn- bind-method
  "Creates "
  [m k]
  (assoc-when
   m
   (m k)
   k (lilactown.react/send-this (m k))))

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


;; Defining components

(defn- static? [method]
  (and (meta method) (:static (meta method))))

(defn- set-statics! [o static-map]
  (doseq [[k v] static-map]
    (set$ o (name k) v))
  ;; return mutated obj
  o)

(defn component
  "Creates a new component factory from a given React component definition.
  `definition` is a map of key-value pairs, where keys are keywords that will
  be used as method names, and values are functions. Methods are automatically
  bound to the component class, and standard React methods automatically are
  passed in `this` as the first argument to them."
  [definition]
  (let [statics (into {} (filter (comp static? second) definition))]
    (-> definition
        (as-> m
            (filter (comp not static? second) m)
          (into {} m))
        (bind-method :getInitialState)
        (bind-method :UNSAFE_componentWillMount)
        (bind-method :componentDidMount)
        (bind-method :shouldComponentUpdate)
        (bind-method :getSnapshotBeforeUpdate)
        (bind-method :componentDidUpdate)
        (bind-method :componentDidCatch)
        (bind-method :componentWillUnmount)
        (bind-method :render)
        (clj->js)
        (create-react-class)
        (set-statics! statics)
        (factory))))

(defn pure-component
  "Creates a new component factory from a given React component definition that
  implements a shallow props equality check."
  [definition]
  (-> definition
      (?assoc
       :shouldComponentUpdate
       (fn [this props state]
         ;; use goog.obj/equals for now to shallow compare
         (or (not (gobj/equals (lilactown.react/this :props) props))
             (not (gobj/equals (lilactown.react/this :state) state)))))
      (component)))

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
                      discerning whether the component should update or not.

   - :async? - a boolean that determines whether to re-render the component
               using `setState` (async, low-priority) or `forceUpdate`
               (immediately). Set to `false` if you are doing e.g. animations
               or other things that HAVE to happen RIGHT NOW. Otherwise, leave
               it defaulted to `true`.

   - :render - render function that is given `this` as it's first argument and
               the map of atoms returned by `:watch` as it's second argument."
  [{:keys [watch init should-update async? displayName render]
    :or {should-update (fn [_ _ _ _] true)
         async? true}
    :as definition}]
  (-> {:displayName (or displayName "ReactiveComponent")

       :getInitialState
       (fn [this]
         (when watch
           #js {:watch (watch this)}))

       :componentDidMount
       (fn [this]
         (let [id (random-uuid)
               update (if async?
                        #(. ^js this setState #js {})
                        #(. ^js this forceUpdate))]
           (lilactown.react/set-this! :watch-id id)
           (t/debug "[reactive]" "Mounting" id)

           (when init
             (t/debug "[reactive]" "Initializing" id)
             (init id this))

           (when (lilactown.react/this :state :watch)
             (let [watches (lilactown.react/this :state :watch)]
               (lilactown.react/set-this! :watched watches)
               (doseq [[k w] watches]
                 (println "adding watch" k w id)
                 (add-watch
                  w
                  id
                  (fn [_k _r old-v new-v]
                    (when (should-update k old-v new-v id)
                      (update)))))))))

       :componentWillUnmount
       (fn [this]
         (t/debug "[reactive]" "Unmounting" (lilactown.react/this :watch-id))
         (when (lilactown.react/this :state :watch)
           (doseq [[k w] (lilactown.react/this :state :watch)]
             (remove-watch w (lilactown.react/this :watch-id)))))}
      (merge definition)
      (merge {:render
              (fn [this]
                (t/debug "[reactive]" "Rendering" (lilactown.react/this :watch-id))
                ((:render definition) this
                 (when watch (lilactown.react/this :state :watch))
                 ;; deref all the atoms in the watch map
                 ;; (when watch
                 ;;   (reduce-kv #(assoc %1 %2 @%3) {} (watch this)))
                 ))})
      (component)))

