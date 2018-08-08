(ns lilactown.react.dom
  (:refer-clojure :exclude [map meta time])
  (:require [lilactown.react :as react])
  (:require-macros [lilactown.react.dom]))

;; (def div (react/factory "div"))

;; (def h1 (react/factory "h1"))

;; (def span (react/factory "span"))

;; (def button (react/factory "button"))

(lilactown.react.dom/make-factories)
