(ns lilactown.react.dom
  (:refer-clojure :exclude [map meta time])
  (:require [lilactown.react :as r]
            [react :as react])
  (:require-macros [lilactown.react.dom]))

(lilactown.react.dom/make-factories)

(def <> (r/factory react/Fragment))
