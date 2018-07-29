(ns lilactown.client.title
  (:require [lilactown.dom :as dom :refer [child-fn]]
            [goog.object :as gobj]
            [goog.functions :refer [debounce]]
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
  (dom/component
   {:displayName "toggle-animate"
    :getInitialState
    (fn [] #js {})

    :componentDidMount
    (fn [this]
      (let [id (random-uuid)]
        (swap-state! assoc id
                     initial-state)
        (add-watch
         !state
         id
         (fn [_k _r old-v new-v]
           (when (not= (old-v id) (new-v id))
             (dom/set-state!
              (fn [_]
                #js {:start (get-in new-v [id :end])
                     :end (get-in new-v [id :start])})))))
        (dom/set-this! :watch id)))

    :componentWillUnmount
    (fn [this]
      (println "Unmounting" (dom/this :watch))
      (remove-watch !state (dom/this :watch)))

    :handleEnter
    (dom/send-this
     []
     (fn [this]
       (let [id (dom/this :watch)]
         (swap-state!
          (fn [cur]
            (assoc
             cur
             id
             {:end (get-in cur [id :start])
              :start (get-in cur [id :end])}))))))

    :render
    (fn [this]
      (let [id (dom/this :watch)
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
                        ;; :opacity 0.5
                        ;; :boxShadow "2px 2px 3px rgba(100, 100, 100, .5)"
                        :border 0}}
               props)
              label))

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
    (dom/div
     {:style {:display "flex"
              :opacity 0.6}}
     (control {:onClick (partial reset-state! :start)} "<")
     (control {:onClick #(swap! !should-change not)}
              (if @!should-change
                "■"
                "▶"))
     (control {:onClick (partial reset-state! :end)} ">")))))

(defn ^:export start! [node]
  (react-dom/render (title) node))
