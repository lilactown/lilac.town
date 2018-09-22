(ns lilactown.workspace.visual-spec
  (:require [lilactown.client.visual-spec :as visual-spec]
            [nubank.workspaces.core :as ws]
            [nubank.workspaces.card-types.react :as ct.react]))



(ws/defcard Editor
  (ct.react/react-card
   (visual-spec/Editor)))
