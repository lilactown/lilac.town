(ns lilactown.site.games.sweeper
  (:require [garden.core :as garden]
            [garden.stylesheet :refer [at-media]]
            [garden.units :refer [px]]
            [lilactown.client.core :as client]))

(def styles
  [[:* {:box-sizing "border-box"}]
   [:body {:font-family "Roboto Condensed, sans-serif"
           :background-color "#DCD0FF"
           :color "#3b3b3b"}]
   [:h1 :h2 :h3 :h4 {:font-family "Roboto Slab, serif"}
    [:small {:font-size "0.7em"
             :display "block"
             :color "#9a549a"
             :margin "-10px 0 0 90px"}]]
   [:a {:color "#371940"
        :text-decoration "none"}
    [:&:hover {:color "#9a549a"}]]
   [:#main {:max-width "670px"
            :margin "40px auto"}]
   [:#sweeper {:background-color "#f9f7ff"
               :padding "0 50px 50px"
               :border-radius "10px"}]])

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
    [:div#main
     [:div {:style "margin: 0 10px"}
      [:a {:href "/"}
       [:h1.title "lilac.town"
        [:small "Games"]]]
      [:div [:h2 "Sweeper"]]]]
    [:div#sweeper
     (client/module {:module :sweeper
                     :init 'lilactown.client.sweeper/start!
                     :ref :parent})]
    [:link {:href "https://use.fontawesome.com/releases/v5.0.6/css/all.css"
            :rel "stylesheet"}]
    [:link {:href "https://fonts.googleapis.com/css?family=Roboto+Condensed|Roboto+Slab"
            :rel "stylesheet"}]
    [:style
     (garden/css styles)]
    (client/main)]])
