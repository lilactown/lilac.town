(ns lilactown.workspace.react
  (:require [nubank.workspaces.core :as ws]
            [nubank.workspaces.card-types.react :as ct.react]
            [lilactown.react :as r]
            [lilactown.react.dom :as dom]
            [lilactown.cursor :as cursor]))

(defn on-change! [ref ev]
  (let [v (.. ev -target -value)]
    (reset! ref v)))

(r/defreactive Input
  :watch (fn [_] {:state (atom "asdf")})
  :render
  (fn [this {:keys [state]}]
    (dom/div
     (dom/div @state)
     (dom/input {:value @state
                 :onChange (partial on-change! state)}))))

(ws/defcard Input
  (ct.react/react-card
   (Input)))


;; Example of a redux-like pattern

(defn select-props [structure]
  (fn [this]
    (reduce-kv
     (fn [m k f]
       (assoc m
              k
              (cursor/select
               (r/get-in$ this "props" (name k)) f)))
     {}
     structure)))

(defmulti dispatch (fn [action ref payload]
                     action))

(defmethod dispatch :foo/update
  [action ref _]
  (swap! ref assoc :foo 42))

(defmethod dispatch :baz/update
  [action ref _]
  (swap! ref update :baz inc))

(r/defrc Foo
  {:watch (select-props
           {:db #(get-in % [:foo :bar])})}
  [{dispatch :dispatch} {state :db}]
  (dom/div
   (dom/div "Foo: " (prn-str @state))
   (dom/button {:onClick #(dispatch :foo/update)}
               "Universe")))

(r/defrc Baz
  {:watch (select-props
           {:db :baz})}
  [{dispatch :dispatch} {state :db}]
  (dom/div
   (dom/div "Baz: " @state)
   (dom/button {:onClick #(dispatch :baz/update)}
               "+")))

(ws/defcard App-db
  (ct.react/react-card
   (let [app-db (atom {:foo {:bar "asdf"}
                       :baz 0})
         dispatcher #(dispatch %1 app-db %2)]
     (dom/div
      (Foo {:db app-db
            :dispatch dispatcher})
      (Baz {:db app-db
            :dispatch dispatcher})))))
