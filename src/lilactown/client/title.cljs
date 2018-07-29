(ns lilactown.client.title
  (:require [lilactown.dom :as dom :refer [child-fn]]
            [react-dom :as react-dom]
            [react-motion :as rm]))

(def motion (dom/factory rm/Motion))

(def !state (atom {}))

(def !should-change (atom true))

(def initial-state {:start 0 :end 2})

(def end-state {:start 2 :end 0})

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

(defn swap-state!
  [& args]
  (when @!should-change
    (apply swap! !state args)))

(defn letter-motion
  [first second on-enter style]
  (let [style (js->clj style :keywordize-keys true)]
    (if (> (:value style) 1)
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

(def toggle-animate
  (dom/reactive-component
   {:displayName "toggle-animate"
    :watch !state
    :init (fn [id]
            (swap-state! assoc id initial-state))
    :should-update
    (fn [id old-v new-v]
      (not= (old-v id) (new-v id)))
    :handleEnter
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
    (fn [this]
      (println "render")
      (let [id (dom/this :watch-id)
            start (or (get-in @!state [id :start])
                      (:start initial-state))
            end (or (get-in @!state [id :end])
                    (:end initial-state))]
        (motion
         {:defaultStyle {:value start}
          :style {:value (rm/spring end)}}
         (partial (dom/children)
                  (dom/this :handleEnter)))))}))

(defn create-letter [[a b]]
  (toggle-animate {:key [a b]}
                  (partial letter-motion a b)))

(defn control [{:keys [on-click] :as props} label]
  (dom/button (merge
               {:onClick on-click}
               {:style {:backgroundColor "rgba(55, 25, 64, .1)"
                        :margin "1px"
                        :padding "2px 5px"
                        :color "#371940"
                        :fontSize "10px"
                        :border 0}}
               props)
              label))

(def controls
  (dom/reactive-component
   {:displayName "Controls"
    :watch !should-change
    :render
    (fn [this]
      (dom/div
       {:style {:display "flex"
                :opacity 0.6}}
       (control {:onClick (partial reset-state! :start)} "<")
       (control {:onClick #(swap! !should-change not)}
                (if @!should-change
                  "■"
                  "▶"))
       (control {:onClick (partial reset-state! :end)} ">")))}))

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
    (controls))))

(defn ^:export start! [node]
  (react-dom/render (title) node))
