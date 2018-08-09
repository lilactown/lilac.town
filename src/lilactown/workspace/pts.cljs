(ns lilactown.workspace.pts
  (:require [nubank.workspaces.core :as ws]
            [nubank.workspaces.card-types.react :as ct.react]
            [lilactown.react :as r]
            [lilactown.react.dom :as dom]
            ["pts" :as pts]))

(r/defcomponent Pts
  :render
  (fn [this]
    (dom/div {:className "pts-chart"}
     (dom/div {:ref #(r/set-this! :pts-canvas %)}))))

(ws/defcard Pts-test
  (ct.react/react-card
   (Pts)))
