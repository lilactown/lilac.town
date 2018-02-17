(ns lilactown.pages.home
  (:require [garden.core :as garden]
            [lilactown.pages.home.data :as data]
            [lilactown.pages.home.format :as format]))

(def styles
  [[:* {:box-sizing "border-box"}]
   [:body {:font-family "Roboto Condensed, sans-serif"
           :background-color "#DCD0FF"
           :color "#3b3b3b"}]
   [:h1 :h2 :h3 :h4 {:font-family "Roboto Slab, serif"}]
   [:a {:color "#371940"
        :text-decoration "none"}
    [:&:hover {:color "#9a549a"}]]
   [:#main {:max-width "650px"
            :margin "40px auto"}]
   ;; [:.title {:text-align "center"}]
   ;; [:.tag-line {:text-align "center"}]
   [:.repos {:display "grid"
             :grid-template-columns "1fr 1fr 1fr"
             :grid-column-gap "15px"
             :grid-row-gap "15px"}
    ]
   [:.repo {;; :border "1px solid rgba(55, 25, 64, .1)"
            :border-radius "4px"
            :padding "10px 12px 10px"
            :background-color "rgba(55, 25, 64, .1)"
            :box-shadow "1px 1px 3px rgba(100, 100, 100, .5)"
            :display "grid"
            :grid-row-gap "8px"
            :grid-auto-rows "minmax(20px, auto)"}
    [:.date {:position "relative"}
     [:.display {:position "absolute"
                 :bottom 0}]]
    ;; [:.title {:padding "0 0 5px 0"}]
    ;; [:.desc {:padding "0 0 8px 0"}]
    [:.stars {:float "right"}]
    [:.star-glyph {:display "inline-block"
                   :vertical-align "text-bottom"
                   :fill "currentColor"
                   :margin-right "5px"}]]
   [:.articles {:list-style "none"
                :padding "0"
                :margin "0"}]
   [:.article {:padding "5px 0"}
    [:.title {:font-size "1.5rem"
              :display "inline-block"}]
    [:.date {:margin-right "12px"
             :font-size "1.2rem"
             :vertical-align "unset"
             :display "inline-block"
             :width "3rem"
             :border-right "1px solid #3b3b3b"
             ;; :text-align "center"
             }]
    [:.categories {:list-style "none"
                   :padding "0"
                   :margin-top "3px"}
     [:.category {:display "inline-block"
                  :text-align "center"
                  :padding "3px 8px"
                  :margin "0 3px 0 0"
                  :background-color "rgba(55, 25, 64, .3)"
                  :border-radius "4px"
                  :color "#DCD0FF"
                  :font-size ".8rem"}
      [:&:hover {:background-color "rgba(55, 25, 64, .7)"}]]]]])

(def star
  [:svg.star-glyph
   {:aria-label "stars"
    :height "18"
    :width "14"
    :role "img"
    :view-box "0 0 14 16"}
   [:path
    {:fill-rule "evenodd"
     :d "M14 6l-4.9-.64L7 1 4.9 5.36 0 6l3.6 3.26L2.67 14 7 11.67 11.33 14l-.93-4.74z"}]])

(defn repo [{:keys [name url description createdAt updatedAt pushedAt stargazers]}]
   [:div.repo
    [:a {:href url :target "_blank"}
     [:div.title [:strong name]
      [:span.stars star (:totalCount stargazers)]]]
    [:div.desc description]
    [:div.date [:div.display [:span.fas.fa-upload] "  " (format/repo-date pushedAt)]]])

(defn article-category [{:keys [name]}]
  [:li.category name])

(defn article [{:keys [title link published-date categories]}]
  [:li.article
   [:div.date (format/article-date published-date)]
   [:div.title
    [:div
     [:span [:a {:href link :target "_blank"} title]]]
    [:ul.categories (map article-category categories)]]])

(defn html []
  (let [{github :github
         articles :medium} (data/fetch' [:github send-git-query]
                                        [:medium medium-feed])
        repos (get-in github [:data :viewer :pinnedRepositories :nodes])]
    [:html
     [:meta {:charset "UTF-8"}]
     [:head
      [:title "Will Acton"]
      [:link {:href "https://use.fontawesome.com/releases/v5.0.6/css/all.css"
              :rel "stylesheet"}]
      [:link {:href "https://fonts.googleapis.com/css?family=Roboto+Condensed|Roboto+Slab"
              :rel "stylesheet"}]
      [:style
       (garden/css styles)]]
     [:body
      [:div#main
       [:h1.title "lilac.town"]
       ;; [:div.tag-line
       ;;  [:p "I develop software of all kinds. Some of it even works!"]]
       [:div
        [:h2 "Open source"]
        [:div.repos
         (map repo repos)]]
       [:div
        [:h2 "Articles"]
        [:ul.articles
         (map article articles)]]]]]))
