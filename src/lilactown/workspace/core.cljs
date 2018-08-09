(ns lilactown.workspace.core
  (:require [nubank.workspaces.core :as ws]
            [lilactown.workspace.title]
            [lilactown.workspace.pts]))

(defonce init (ws/mount))
