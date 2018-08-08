(ns lilactown.workspace.core
  (:require [nubank.workspaces.core :as ws]
            [lilactown.workspace.title :as title]))

(defonce init (ws/mount))
