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

(defonce app-db (atom {:foo "bar"
                       :baz 0}))

(def foo-db (cursor/select app-db :foo))

(def baz-db (cursor/select app-db :baz))

(r/defreactive Foo
  :watch (fn [_] {:state foo-db})
  :render
  (fn [this {state :state}]
    (dom/div
     (dom/div "Foo: " @state)
     (dom/button {:onClick #(swap! app-db assoc :foo "42")}
                 "Universe"))))

(r/defreactive Baz
  :watch (fn [_] {:state baz-db})
  :render
  (fn [this {state :state}]
    (dom/div
     (dom/div "Baz: " @state)
     (dom/button {:onClick #(swap! app-db update :baz inc)}
                 "+"))))

(ws/defcard App-db
  (ct.react/react-card
   (dom/div
    (Foo)
    (Baz))))
