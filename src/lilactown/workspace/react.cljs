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

(defmulti dispatch (fn [action ref payload]
                     action))

(defmethod dispatch :foo/update
  [action ref _]
  (swap! ref assoc :foo 42))

(defmethod dispatch :baz/update
  [action ref _]
  (swap! ref update :baz inc))

(r/defrc Foo
  {:watch (fn [this]
            {:state (-> (r/props :app-db)
                        (cursor/select :foo))})}
  [{dispatch :dispatch} {state :state}]
  (dom/div
   (dom/div "Foo: " @state)
   (dom/button {:onClick #(dispatch :foo/update)}
               "Universe")))

(r/defrc Baz
  {:watch (fn [this]
            {:state (-> (r/props :app-db)
                        (cursor/select :baz))})}
  [{dispatch :dispatch} {state :state}]
  (dom/div
   (dom/div "Baz: " @state)
   (dom/button {:onClick #(dispatch :baz/update)}
               "+")))

(ws/defcard App-db
  (ct.react/react-card
   (let [app-db (atom {:foo "bar"
                       :baz 0})
         dispatcher #(dispatch %1 app-db %2)]
     (dom/div
      (Foo {:app-db app-db
            :dispatch dispatcher})
      (Baz {:app-db app-db
            :dispatch dispatcher})))))
