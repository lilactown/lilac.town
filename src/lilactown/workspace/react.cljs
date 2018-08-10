(ns lilactown.workspace.react
  (:require [nubank.workspaces.core :as ws]
            [nubank.workspaces.card-types.react :as ct.react]
            [lilactown.react :as r]
            [lilactown.react.dom :as dom]))

(defn on-change! [ref ev]
  (let [v (.. ev -target -value)]
    (println v)
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
