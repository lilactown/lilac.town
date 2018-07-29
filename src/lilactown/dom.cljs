(ns lilactown.dom
  (:require [react :as react]
            [create-react-class :as create-react-class])
  (:require-macros [lilactown.dom]))

(defn component
  [schema]
  (create-react-class (clj->js schema)))

(defn factory [component]
  (let [f (react/createFactory component)]
    (fn [props & children]
      (if (map? props)
        (apply f (clj->js props) children)

        ;; props are children
        (apply f nil props children)))))

(def div (factory "div"))

(def h1 (factory "h1"))

(def span (factory "span"))

(def button (factory "button"))
