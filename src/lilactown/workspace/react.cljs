(ns lilactown.workspace.react
  (:require [nubank.workspaces.core :as ws]
            [nubank.workspaces.card-types.react :as ct.react]
            [react :as react]
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

(defn db-provider [Provider]
  (r/reactive-component
   {:watch (fn [this]
             {:db (r/props :db)})
    :render
    (fn [this {db :db}]
      (Provider {:value {:db @db
                         :dispatch (r/props :dispatch)}}
                (r/children)))}))

(defn db-context
  ([]
   (let [context (react/createContext)
         Provider (db-provider (r/factory (.-Provider context)))
         Consumer (r/factory (.-Consumer context))]
     [Provider Consumer])))

(defmulti dispatch (fn [ref action payload]
                     action))

(defmethod dispatch :name/update
  [ref action payload]
  (swap! ref assoc :name payload))

(defmethod dispatch :count/inc
  [ref action _]
  (swap! ref update :count inc))

(def my-context (db-context))

(r/defnc Foo
  [{:keys [name on-change]}]
  (println "Foo")
  (dom/div
   (dom/div "Greetings, " name)
   (dom/input {:value name
               :onChange #(on-change (.. % -target -value))})))

(r/defnc FooContainer []
  (let [[_ DbConsumer] my-context]
    (DbConsumer
     (fn [{db :db
           dispatch :dispatch}]
       (Foo {:name (:name db)
             :on-change ^:no-update #(dispatch :name/update %)})))))

(r/defnc Bar
  [{:keys [count on-click]}]
  (println "Bar")
  (dom/div
   (dom/div "Count: " count)
   (dom/button {:onClick #(on-click)}
               "inc")))

(r/defnc BarContainer []
  (let [[_ DbConsumer] my-context]
    (DbConsumer
     (fn [{db :db dispatch :dispatch}]
       (Bar {:count (:count db)
             :on-click ^:no-update #(dispatch :count/inc)})))))

(ws/defcard App-db
  (ct.react/react-card
   (let [[DbProvider _] my-context
         db (atom {:name "wat"
                   :count 0})
         dispatch (partial dispatch db)]
     (DbProvider
      {:db db
       :dispatch dispatch}
      (dom/div
       (FooContainer)
       (BarContainer))))))
