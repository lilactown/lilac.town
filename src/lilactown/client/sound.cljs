(ns lilactown.client.sound
  (:require [lilactown.react :as r]
            [lilactown.react.dom :as dom]))


(r/defnc Container []
  (dom/div
   (dom/script {:src "https://connect.soundcloud.com/sdk/sdk-3.3.0.js"})
   (dom/div "hi")))
