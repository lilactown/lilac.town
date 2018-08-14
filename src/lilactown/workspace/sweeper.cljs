(ns lilactown.workspace.sweeper
  (:require [lilactown.client.sweeper :as sweeper]
            [nubank.workspaces.core :as ws]
            [nubank.workspaces.card-types.react :as ct.react]))

(ws/defcard Sweeper
  (ct.react/react-card
   (sweeper/Container)))
