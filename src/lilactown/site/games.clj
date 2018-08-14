(ns lilactown.site.games
  (:require [garden.core :as garden]
            [garden.stylesheet :refer [at-media]]
            [garden.units :refer [px]]
            [lilactown.client.core :as client]))

(def styles
  [[:* {:box-sizing "border-box"}]
   [:body {:font-family "Roboto Condensed, sans-serif"
           :background-color "#DCD0FF"
           :color "#3b3b3b"}]
   [:h1 :h2 :h3 :h4 {:font-family "Roboto Slab, serif"}]
   [:a {:color "#371940"
        :text-decoration "none"}
    [:&:hover {:color "#9a549a"}]]])

(defn render []
  [:html
   [:meta {:charset "UTF-8"}]
   [:meta {:name "viewport" :content "width=device-width,initial-scale=1"}]
   [:head
    [:title "Will Acton"]
    [:link {:href "https://use.fontawesome.com/releases/v5.0.6/css/all.css"
            :rel "preload"
            :as "style"}]
    [:link {:href "https://fonts.googleapis.com/css?family=Roboto+Condensed|Roboto+Slab"
            :rel "preload"
            :as "style"}]]
   [:body
    "Hi"
    [:style
     (garden/css styles)]]])
