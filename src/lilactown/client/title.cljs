(ns lilactown.client.title
  (:require [lilactown.react.dom :as dom]
            [lilactown.react :as r]
            [taoensso.timbre :as t]
            [react-dom :as react-dom]
            [react-motion :as rm]))

(def Motion (r/factory rm/Motion))

(def !state (atom {}))

(defonce !should-change (atom true))

(def initial-state {:start 0 :end 2})

(def end-state {:start 2 :end 0})

(def halfway (/ (- (:end initial-state) (:start initial-state)) 2))

(defn reset-letters!
  [key]
  (swap! !state
         (fn [cur]
           (into
            {}
            (map (fn [[k v]]
                   [k (case key
                        :start initial-state
                        :end end-state)])
                 cur)))))

(defn initial-letters! [id]
  (swap! !state assoc id initial-state))

(defn swap-letters!
  [& args]
  (when @!should-change
    (apply swap! !state args)))

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

(r/defreactive ToggleAnimate
  :watch (fn [this] {:letter-state !state})
  :init (fn [id]
          (initial-letters! id))
  :should-update
  (fn [_ old-v new-v id]
    (not= (old-v id) (new-v id)))
  :handle-enter
  (r/send-this
   (fn [this]
     (let [id (r/this :watch-id)]
       (swap-letters!
        (fn [cur]
          (assoc
           cur
           id
           {:end (get-in cur [id :start])
            :start (get-in cur [id :end])}))))))
  :render
  (fn [this {:keys [letter-state]}]
    (let [id (r/this :watch-id)
          start (or (get-in @letter-state [id :start])
                    (:start initial-state))
          end (or (get-in @letter-state [id :end])
                  (:end initial-state))]
      (Motion
       {:defaultStyle {:value start}
        :style {:value (rm/spring end)}}
       (partial (r/children)
                (r/this :handle-enter))))))

(defn create-letter [[a b]]
  (ToggleAnimate {:key [a b]}
                 (partial letter a b)))

(r/defnc Control [{on-click :on-click label :children :as props}]
  (dom/button (merge
               {:onClick on-click
                :className "control"}
               props)
              label))

(r/defreactive Controls
  :watch (fn [this] {:!should-change? !should-change})
  :reset-end (fn [_] (reset-letters! :end))
  :reset-start (fn [_] (reset-letters! :start))
  :toggle-change (fn [_] (swap! !should-change not))
  :render
  (fn [this {:keys [!should-change?]}]
    (dom/div
     {:style {:display "flex"
              :opacity 0.6}}
     (Control {:onClick (r/this :reset-end)} "<")
     (Control {:onClick (r/this :toggle-change)}
              (if @!should-change?
                "■"
                "▶"))
     (Control {:onClick (r/this :reset-start)} ">"))))

(r/defnc Title []
  (dom/div
   {:style {:position "relative"}}
   (dom/h1
    (map create-letter [["w" "l"]
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
    (Controls))))

(defn ^{:export true} start! [node]
  (t/info "Title started")
  (react-dom/render (Title) node))
