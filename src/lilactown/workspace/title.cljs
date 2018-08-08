(ns lilactown.workspace.title
  (:require [lilactown.client.title :as title]
            [nubank.workspaces.core :as ws]
            [nubank.workspaces.card-types.react :as ct.react]))

(ws/defcard Title
  (ct.react/react-card
   (title/Title)))
