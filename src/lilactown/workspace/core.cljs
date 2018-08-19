(ns lilactown.workspace.core
  (:require [nubank.workspaces.core :as ws]
            [lilactown.workspace.title]
            [lilactown.workspace.pts]
            [lilactown.workspace.react]
            [lilactown.workspace.sweeper]
            [lilactown.workspace.sound]))


(defonce init (ws/mount))
