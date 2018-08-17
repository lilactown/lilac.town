(ns lilactown.site.home
  (:require [garden.core :as garden]
            [garden.stylesheet :refer [at-media]]
            [garden.units :refer [px]]
            [lilactown.client.core :as client]
            [clj-time.format :as f]
            [clj-time.coerce :as coerce]))

(defn article-date [d]
  (f/unparse (f/formatter "MMM YYYY") (clj-time.coerce/from-long d)))

(def styles
  [[:* {:box-sizing "border-box"}]
   [:body {:font-family "Roboto Condensed, sans-serif"
           :background-color "#fbfbfb"
           :color "#3b3b3b"}]
   [:h1 :h2 :h3 :h4 {:font-family "Roboto Slab, serif"}]
   [:a {:color "#371940"
        :text-decoration "none"}
    [:&:hover {:color "#9a549a"}]]
   [:#version
    {:position "fixed"
     :bottom "0"
     :right "0"
     :font-size "0.7em"
     :opacity "0.6"
     :padding-bottom "5px"
     :padding-right "10px"}
    (at-media
     {:screen true :max-width (px 425)}
     [:& {:float "right"
          :padding-top "10px"
          :position "initial"}])]
   [:#main {:max-width "670px"
            :margin "40px auto"}]
   [:.repos {:display "grid"
             :grid-template-columns "1fr 1fr 1fr"
             :grid-column-gap "15px"
             :grid-row-gap "15px"}
    (at-media {:screen true :max-width (px 991)}
              [:& {:grid-template-columns "1fr 1fr"}])
    (at-media {:screen true :max-width (px 425)}
              [:& {:grid-template-columns "1fr"}])]
   [:.repo {:border-radius "4px"
            :padding "10px 12px 10px"
            :border "1px solid #eee"
            ;; :background-color "rgba(55, 25, 64, .1)"
            :background-color "#f9f7ff"
            :box-shadow "2px 2px 3px rgba(100, 100, 100, .5)"
            :display "grid"
            :grid-row-gap "8px"
            :grid-auto-rows "minmax(20px, auto)"}
    [:.top {:margin "-11px -13px 0px"
            :height "2.4em"
            :padding "10px"
            :background-color "#e8d9fb"
            :border-top-left-radius "4px"
            :border-top-right-radius "4px"}]
    [:.bottom {:position "relative"}
     [:.display {:position "absolute"
                 :bottom 0}]]
    [:.stars {:float "right"}]
    [:.star-glyph {:display "inline-block"
                   :vertical-align "text-bottom"
                   :fill "currentColor"
                   :margin-right "5px"}]
    [:.lang
     [:.repo-lang-circle {:display "inline-block";
                          :width "12px";
                          :height "12px";
                          :border-radius "50%"
                          :position "relative"
                          :top "1px"
                          :background "black"}]]]
   [:.articles {:list-style "none"
                :padding "0"
                :margin "0"}]
   [:.article {:padding "5px 0"
               :display "flex"}
    [:.title {:font-size "1.5rem"
              :flex 1}]
    [:.date {:margin-right "12px"
             :font-size "1.2rem"
             :vertical-align "unset"
             :display "inline-block"
             :width "3rem"
             :border-right "1px solid #3b3b3b"}]
    [:.categories {:list-style "none"
                   :padding "0"
                   :margin "0"}
     [:.category {:display "inline-block"
                  :text-align "center"
                  :padding "3px 8px"
                  :margin "3px 3px 0 0"
                  :background-color "rgba(55, 25, 64, .3)"
                  :border-radius "4px"
                  :color "#f8f4ff"
                  :font-size ".8rem"}
      [:&:hover {:background-color "rgba(55, 25, 64, .7)"}]]]]
   [:.control
    {:background-color "rgba(55, 25, 64, .1)"
     :margin "1px"
     :padding "2px 5px"
     :color "#371940"
     :font-size "10px"
     :border 0}
    [:&:active
     {:background-color "rgba(55, 25, 64, 0.4)"
      :color "white"}]]])

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

(def lang-color
  {"JavaScript" "#575cff"
   "OCaml" "#d417a2"
   "Clojure" "#9a549a"})

(defn repo [{:keys [name url description createdAt updatedAt pushedAt stargazers
                    primaryLanguage]}]
  [:div.repo
   [:div.top
    [:a {:href url :target "_blank"}
     [:div.title [:strong name]
      [:span.stars star (:totalCount stargazers)]]]]
   [:div.desc description]
   [:div.bottom
    [:div.display
     [:div.lang
      [:span.repo-lang-circle {:style (str "background: "
                                           (get lang-color
                                                (:name primaryLanguage)
                                                "black"))}]
      "  " (:name primaryLanguage)]]]])

(defn article-category [name]
  [:li.category name])

(defn article [{:keys [title link created-at tags claps]}]
  [:li.article
   [:div.date (article-date created-at)]
   [:div.title
    [:div.text
     [:span [:a {:href link :target "_blank"}
             title]]]
    [:ul.categories (map article-category tags) [:li.category [:span.fas.fa-thumbs-up] " " claps]]]])

(defn render [{:keys [github medium version]}]
  (let [repos (get-in github [:data :viewer :pinnedRepositories :nodes])]
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
        [:div#title
         [:h1.title "lilac.town"]]
        (client/module {:module :title
                        :init 'lilactown.client.title/start!
                        :data {:testing 123}
                        :ref "#title"})
       ;; [:div.tag-line
       ;;  [:p "I develop software of all kinds. Some of it even works!"]]
        [:div
         [:h2 "Open source"]
         [:div.repos
          (map repo repos)]]
        [:div
         [:h2 "Articles"]
         [:ul.articles
          (map article medium)]]
        [:div
         [:h2 "Games"]
         [:div.repos
          [:div.repo
           [:a {:href "/games/sweeper"}
            [:div.top [:strong "Sweeper"]]
            [:img {:src "/assets/images/sweeper.png"
                  :style "width: 100%; border-radius: 4px"}]]]]]]
       [:div#version
        [:a
         {:href (str "https://github.com/Lokeh/lilac.town/commit/" version)}
         version]]]
      [:link {:href "https://use.fontawesome.com/releases/v5.0.6/css/all.css"
              :rel "stylesheet"}]
      [:link {:href "https://fonts.googleapis.com/css?family=Roboto+Condensed|Roboto+Slab"
              :rel "stylesheet"}]
      [:style
       (garden/css styles)]
      (client/main)]]))
