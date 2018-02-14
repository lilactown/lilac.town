(ns lilactown.pages.home
  (:require [garden.core :as garden]))

(def styles [[:body {:font-family "Roboto Condensed, sans-serif"
                     :background-color "#C8A2C8"
                     :color "#3b3b3b"}]
             [:h1 :h2 :h3 :h4 {:font-family "Roboto Slab, serif"}]
             [:#main {:max-width "600px"
                      :margin "40px auto"}]
             ;; [:.title {:text-align "center"}]
             ;; [:.tag-line {:text-align "center"}]
             [:.repos {:display "grid"
                      :grid-template-columns "1fr 1fr 1fr"}]
             ])

;; (garden/css styles)

(defn html []
  [:html
   [:head
    [:title "Will Acton"]
    [:link {:href "https://fonts.googleapis.com/css?family=Roboto+Condensed|Roboto+Slab"
            :rel "stylesheet"}]
    [:style
     (garden/css styles)]]
   [:body
    [:div#main
     [:h1.title "lilac.town"]
     [:div.tag-line
      [:p "I develop software of all kinds. Some of it even works!"]]
     [:div
      [:h2 "Pinned repositories"]
      [:div.repos
       [:div.repo 1]
       [:div.repo 2]
       [:div.repo 3]
       [:div.repo 4]
       [:div.repo 5]
       [:div.repo 6]
       [:div.repo 7]]]]]])
