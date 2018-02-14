(ns lilactown.pages.home
  (:require [garden.core :as garden]
            [clj-http.client :as http]
            [cheshire.core :as c]
            [clojure.string :as s]
            [clj-time.format :as f]))

(def styles [[:* {:box-sizing "border-box"}]
             [:body {:font-family "Roboto Condensed, sans-serif"
                     :background-color "#C8A2C8"
                     :color "#3b3b3b"}]
             [:h1 :h2 :h3 :h4 {:font-family "Roboto Slab, serif"}]
             [:#main {:max-width "650px"
                      :margin "40px auto"}]
             ;; [:.title {:text-align "center"}]
             ;; [:.tag-line {:text-align "center"}]
             [:.repos {:display "grid"
                       :grid-template-columns "1fr 1fr 1fr"
                       :grid-column-gap "15px"
                       :grid-row-gap "15px"}
              [:a {:text-decoration "none"
                     :color "#3b3b3b"}]]
             [:.repo {:border "1px solid #555"
                      :border-radius "4px"
                      :padding "10px 12px 10px"
                      :background-color "rgba(100, 100, 100, .1)"
                      :box-shadow "1px 1px 3px rgba(0, 0, 0, .5)"
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
                             :margin-right "5px"}]]])

(def query "Pinned repos"
  (s/replace
   "query { 
  viewer { 
    pinnedRepositories(first: 6) {
      nodes {
        name
        url
        description
        stargazers {
          totalCount
        }
        createdAt
        updatedAt
        pushedAt
        primaryLanguage {
          name
        }
      }
    }
  }
}" #"\n" ""))

(defn format-date [s]
  (let [->f (f/formatters :date-time-no-ms)
        <-f (f/formatter "MMM YYYY")
        ]
    (->> s
        (f/parse ->f)
        (f/unparse <-f))))

(defn send-query []
  (c/parse-string
   (:body (http/post "https://api.github.com/graphql"
                     {:headers {"Authorization" "bearer "}
                      :body (str "{\"query\": \"" query "\"}")}))
   true))

;; (def query {:viewer {:pinnedRepositories {:first 6}
;;                      {:nodes [:name
;;                               :url
;;                               :description
;;                               {:stargazers [:totalCount]}
;;                               :createdAt]}}})

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
    [:div.date [:div.display [:span.fas.fa-upload] "  " (format-date pushedAt)]]])

(defn html []
  (let [data (send-query)
        repos (get-in data [:data :viewer :pinnedRepositories :nodes])]
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
         (map #(repo %) repos)]]
       [:div
        [:h2 "Articles"]]
       [:div
        [:h2 "Cool stuff"]]]]]))
