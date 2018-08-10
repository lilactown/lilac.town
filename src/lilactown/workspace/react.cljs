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

(def app-db (atom {:foo "bar"
                       :baz 0}))

(r/defrc Foo
  {:watch (fn [this]
            {:state (cursor/select (r/props :app-db) :foo)})}
  [_ {state :state}]
  (dom/div
   (dom/div "Foo: " @state)
   (dom/button {:onClick #(swap! app-db assoc :foo "42")}
               "Universe")))

(r/defrc Baz
  {:watch (fn [this]
            {:state (cursor/select (r/props :app-db) :baz)})}
  [_ {state :state}]
  (dom/div
   (dom/div "Baz: " @state)
   (dom/button {:onClick #(swap! app-db update :baz inc)}
               "+")))

(ws/defcard App-db
  (ct.react/react-card
   (dom/div
    (Foo {:app-db app-db})
    (Baz {:app-db app-db}))))
