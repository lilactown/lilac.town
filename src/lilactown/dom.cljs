(ns lilactown.dom
  (:require [react :as react]
            [goog.object :as gobj]
            [taoensso.timbre :as t :include-macros true]
            [create-react-class :as create-react-class])
  (:require-macros [lilactown.dom]))

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
   k (lilactown.dom/send-this [] (m k))))

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
  (factory
   (create-react-class
    (-> definition
        (bind-method :getInitialState)
        (bind-method :componentDidMount)
        (bind-method :componentWillUnmount)
        (bind-method :render)
        (clj->js)))))

(defn reactive-component
  "Creates a new ReactiveComponent factory from a given React component
  definition."
  [{:keys [watch init should-update
           display-name]
    :or {should-update (fn [_ _ _] true)}
    :as definition}]
  (-> {:displayName (or display-name "ReactiveComponent")
       :getInitialState
       (fn [] #js {})

       :componentDidMount
       (fn [this]
         (let [id (random-uuid)]
           (t/debug "[reactive]" "Mounting" id)
           (when init (init id this))
           (when watch
             (doseq [w watch]
               (add-watch
                w
                id
                (fn [_k _r old-v new-v]
                  (when (should-update id old-v new-v)
                    (lilactown.dom/set-state!
                     (fn [_]
                       #js {:triggered true})))))))
           (lilactown.dom/set-this! :watch-id id)))

       :componentWillUnmount
       (fn [this]
         (t/debug "[reactive] Unmounting" (lilactown.dom/this :watch-id))
         (when watch
           (doseq [w watch]
             (remove-watch w (lilactown.dom/this :watch-id)))))}
      (merge definition)
      (component)))

(def div (factory "div"))

(def h1 (factory "h1"))

(def span (factory "span"))

(def button (factory "button"))
