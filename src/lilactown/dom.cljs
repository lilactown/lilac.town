(ns lilactown.dom
  (:require [react :as react]
            [goog.object :as gobj]
            [taoensso.timbre :as t :include-macros true]
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

(defn reactive-component
  [{:keys [watch init should-update]
    :or {should-update (fn [_ _ _] true)}
    :as schema}]
  (-> {:getInitialState
       (fn [] #js {})

       :componentDidMount
       (fn [this]
         (let [id (random-uuid)]
           ;; [println "[reactive] Mounting" id]
           (t/debug "[reactive]" "Mounting" id)
           (when init (init id this))
           (when watch
             (add-watch
              watch
              id
              (fn [_k _r old-v new-v]
                (when (should-update id old-v new-v)
                  (lilactown.dom/set-state!
                   (fn [_]
                     #js {:triggered true}))))))
           (lilactown.dom/set-this! :watch-id id)))

       :componentWillUnmount
       (fn [this]
         (t/debug "[reactive] Unmounting" (lilactown.dom/this :watch-id))
         (when watch
           (remove-watch watch (lilactown.dom/this :watch-id))))}
      (merge schema)
      (component)))

(def div (factory "div"))

(def h1 (factory "h1"))

(def span (factory "span"))

(def button (factory "button"))
