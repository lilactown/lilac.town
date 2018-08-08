(ns lilactown.client.title
  (:require [lilactown.react.dom :as dom]
            [lilactown.react :as r]
            [taoensso.timbre :as t]
            [react :as react]
            [react-dom :as react-dom]
            [react-motion :as rm]
            ["util" :as util]))

;; Setup

(def ^:const initial-state {:start 0 :end 2})

(def ^:const end-state {:start 2 :end 0})

(def halfway (/ (- (:end initial-state) (:start initial-state)) 2))


;; Biz logic

(defn reset-letters!
  [!state key]
  (swap! !state
         (fn [cur]
           (into
            {}
            (map (fn [[k v]]
                   [k (case key
                        :start initial-state
                        :end end-state)])
                 cur)))))

(defn initial-state! [state id]
  (swap! state assoc id initial-state))

(defn swap-state!
  [state should-change? & args]
  (when should-change?
    (apply swap! state args)))


;; React stuff

(def Motion (r/factory rm/Motion))

(def toggle-context (.createContext react))

(def ToggleProvider
  (r/factory (.-Provider toggle-context)))

(def ToggleConsumer
  (let [Consumer (r/factory (.-Consumer toggle-context))
        Watch (r/reactive-component
               {:displayName "ToggleWatch"
                :watch (fn [this] {:av (r/props :value)})

                :render
                (fn [this]
                  ((r/children) (r/props :value)))})]
    (r/component
     {:displayName "ToggleConsumer"
      :render
      (fn [this _]
        (Consumer
         (fn [value]
           (Watch {:value value}
                  (fn [value]
                    ((r/children) value))))))})))

(def letters-context (.createContext react))

(def LettersProvider
  (r/factory (.-Provider letters-context)))

(def LettersConsumer
  (let [Consumer (r/factory (.-Consumer letters-context))
        Watch (r/reactive-component
               {:displayName "LettersWatch"
                :watch (fn [this] {:letter-state (r/props :value)})
                :init (fn [id this]
                        (initial-state! (r/props :value) id))
                :should-update
                (fn [_ old-v new-v id]
                  (not= (old-v id) (new-v id)))
                :render
                (fn [this _]
                  ((r/children) {:value (r/props :value)
                                 :watch-id (r/this :watch-id)}))})]
    (r/component
     {:displayName "LettersConsumer"
      :render
      (fn [this]
        (Consumer
         (fn [value]
           (Watch
            {:value value}
            (fn [{:keys [value watch-id] :as blah}]
              ((r/children) {:state value
                             :watch-id watch-id}))))))})))


;; Render

(defn letter
  [first second on-enter style]
  (let [style (js->clj style :keywordize-keys true)]
    (if (> (:value style) halfway)
      (dom/span {:className "title"
                 :onMouseEnter on-enter
                 :style {:transform (str "rotate(" (/ (:value style) 2) "turn)")
                         :display "inline-block"}}
                first)

      (dom/span {:className "title"
                 :onMouseEnter on-enter
                 :style {:transform (str "rotate(" (:value style) "turn)")
                         :display "inline-block"}}
                second))))

(r/defcomponent ToggleAnimate
  :handle-enter
  (r/send-this
   (fn [this]
     (let [id (r/props :watch-id)
           state (r/props :state)]
       (swap-state!
        state
        (r/props :should-change?)
        (fn [cur]
          (assoc
           cur
           id
           {:end (get-in cur [id :start])
            :start (get-in cur [id :end])}))))))

  :render
  (fn [this]
    (let [letter-state @(r/props :state)
          id (r/props :watch-id)
          start (or (get-in letter-state [id :start])
                    (:start initial-state))
          end (or (get-in letter-state [id :end])
                  (:end initial-state))]
      (Motion
       {:defaultStyle {:value start}
        :style {:value (rm/spring end)}}
       (partial (r/children)
                (r/this :handle-enter))))))

(defn create-letter [[a b]]
  (LettersConsumer
   {:key [a b]}
   (fn [{:keys [state watch-id]}]
     (ToggleConsumer
      (fn [should-change?]
        (ToggleAnimate {:state state
                        :watch-id watch-id
                        :should-change? @should-change?}
                       (partial letter a b)))))))

(r/defnc Control [{on-click :on-click label :children :as props}]
  (dom/button (merge
               {:className "control"}
               props)
              label))

(defn controls []
  (ToggleConsumer
   (fn [should-change?]
     (LettersConsumer
      (fn [{letters-state :state}]
        (dom/div
         {:style {:display "flex"
                  :opacity 0.6}}
         (Control {:onClick (partial reset-letters! letters-state :end)} "<")
         (Control {:onClick #(swap! should-change? not)}
                  (if @should-change?
                    "■"
                    "▶"))
         (Control {:onClick (partial reset-letters! letters-state :start)} ">")))))))


(r/defnc Title []
  (dom/div
   {:style {:position "relative"}}
   (dom/h1
    (map create-letter
         [["w" "l"]
          ["i" "i"]
          ["l" "l"]
          ["l" "a"]
          ["." "c"]
          ["a" "."]
          ["c" "t"]
          ["t" "o"]
          ["o" "w"]
          ["n" "n"]]))
   (dom/div
    {:style {:position "absolute"
             :bottom -20
             :left 92}}
    (controls))))

(defn ^{:export true} start! [node]
  (t/info "Title started")
  (react-dom/render
   (->> (Title)
        (LettersProvider {:value (atom {})})
        (ToggleProvider {:value (atom true)}))
   node))
