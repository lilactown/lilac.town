(ns lilactown.site.projects.visual-spec
  (:require [garden.core :as garden]
            [garden.stylesheet :refer [at-media]]
            [garden.units :refer [px]]
            [lilactown.client.core :as client]))

(def styles
  [[:* {:box-sizing "border-box"}]
   [:body {:font-family "Roboto Condensed, sans-serif"
           :background-color "#fbfbfb"
           :color "#3b3b3b"}]
   [:h1 :h2 :h3 :h4 {:font-family "Roboto Slab, serif"}
    [:small {:font-size "0.7em"
             :display "block"
             :color "#9a549a"
             :margin "-10px 0 0 90px"}]]
   [:a {:color "#371940"
        :text-decoration "none"}
    [:&:hover {:color "#9a549a"}]]
   [:.title {:margin-bottom "10px"}]
   [:#main {:max-width "670px"
            :margin "40px auto 20px"}]
   [:#sweeper ]])

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
        [:small "Projects"]]]
      [:div [:h2 {:style "margin: 0"}"Visual Spec"]]]]
    [:div#app]
    (client/module {:module :visual-spec
                    :init 'lilactown.client.visual-spec/start!
                    :ref "#app"})
    [:link {:href "https://use.fontawesome.com/releases/v5.0.6/css/all.css"
            :rel "stylesheet"}]
    [:link {:href "https://fonts.googleapis.com/css?family=Roboto+Condensed|Roboto+Slab"
            :rel "stylesheet"}]
    [:link {:rel "stylesheet" :href "/assets/css/codemirror.css"}]
    [:style
     (garden/css styles)]
    (client/main)]])
