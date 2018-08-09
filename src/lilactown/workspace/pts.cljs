(ns lilactown.workspace.pts
  (:require [nubank.workspaces.core :as ws]
            [nubank.workspaces.card-types.react :as ct.react]
            [lilactown.client.pts :as pts]))

(ws/defcard Pts-test
  (ct.react/react-card
   (pts/Pts)))
