(ns lilactown.workspace.sound
  (:require [nubank.workspaces.core :as ws]
            [nubank.workspaces.card-types.react :as ct.react]
            [lilactown.client.sound :as sound]))

(ws/defcard Sound
  (ct.react/react-card
   (sound/Container)))
