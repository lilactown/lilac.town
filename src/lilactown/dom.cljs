(ns lilactown.dom
  (:require [react :as react]
            [goog.object :as gobj]
            [create-react-class :as create-react-class])
  (:require-macros [lilactown.dom]))

(defn factory [component]
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

(defn props* [this & keys]
  (apply get-in$ this "props" keys))

(defn children* [this]
  (props* this "children"))

(defn state* [this & keys]
  (apply get-in$ "state" keys))

(defn component
  [schema]
  (factory
   (create-react-class
    (-> schema
        (bind-method :getInitialState)
        (bind-method :componentDidMount)
        (bind-method :componentWillUnmount)
        (bind-method :render)
        (clj->js)))))

(def div (factory "div"))

(def h1 (factory "h1"))

(def span (factory "span"))

(def button (factory "button"))
