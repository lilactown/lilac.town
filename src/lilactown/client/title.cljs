(ns lilactown.client.title
  (:require [lilactown.dom :as dom]
            [taoensso.timbre :as t]
            [react-dom :as react-dom]
            [react-motion :as rm]))

(def Motion (dom/factory rm/Motion))

(def !state (atom {}))

(defonce !should-change (atom true))

(def initial-state {:start 0 :end 2})

(def end-state {:start 2 :end 0})

(def halfway (/ (- (:end initial-state) (:start initial-state)) 2))

(defn reset-state!
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

(defn initial-state! [id]
  (swap! !state assoc id initial-state))

(defn swap-state!
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

(def ToggleAnimate
  (dom/reactive-component
   {:displayName "ToggleAnimate"
    :watch {:letter-state !state}
    :init (fn [id]
            (initial-state! id))
    :should-update
    (fn [_ old-v new-v id]
      (not= (old-v id) (new-v id)))
    :handle-enter
    (dom/send-this
     []
     (fn [this]
       (let [id (dom/this :watch-id)]
         (swap-state!
          (fn [cur]
            (assoc
             cur
             id
             {:end (get-in cur [id :start])
              :start (get-in cur [id :end])}))))))
    :render
    (fn [this {:keys [letter-state]}]
      (let [id (dom/this :watch-id)
            start (or (get-in letter-state [id :start])
                      (:start initial-state))
            end (or (get-in letter-state [id :end])
                    (:end initial-state))]
        (Motion
         {:defaultStyle {:value start}
          :style {:value (rm/spring end)}}
         (partial (dom/children)
                  (dom/this :handle-enter)))))}))

(defn create-letter [[a b]]
  (ToggleAnimate {:key [a b]}
                  (partial letter a b)))

(defn control [{:keys [on-click] :as props} label]
  (dom/button (merge
               {:onClick on-click
                :className "control"}
               props)
              label))

(def Controls
  (dom/reactive-component
   {:displayName "Controls"
    :watch {:should-change? !should-change}
    :render
    (fn [this {:keys [should-change?]}]
      (dom/div
       {:style {:display "flex"
                :opacity 0.6}}
       (control {:onClick (partial reset-state! :end)} "<")
       (control {:onClick #(swap! !should-change not)}
                (if should-change?
                  "■"
                  "▶"))
       (control {:onClick (partial reset-state! :start)} ">")))}))

(defn title []
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
  (react-dom/render (title) node))
