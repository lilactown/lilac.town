(ns lilactown.pages.home
  (:require [lilactown.config :as config]
            [garden.core :as garden]
            [clj-http.client :as http]
            [cheshire.core :as c]
            [clojure.string :as s]
            [clj-time.format :as f]
            [clj-time.coerce]
            [feedparser-clj.core :as feed]
            [clojure.core.async :as async]
            [clojure.java.io :as io]))

(defn styles [bg-color]
  [[:* {:box-sizing "border-box"}]
   [:body {:font-family "Roboto Condensed, sans-serif"
           :background-color bg-color
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
                  :background-color "rgba(0, 0, 0, .5)"
                  :border-radius "4px"
                  :color "#DCD0FF"
                  :font-size ".8rem"}]]]])

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

(defn format-repo-date [s]
  (let [->f (f/formatter (f/formatters :date-time-no-ms))
        <-f (f/formatter "MMM YYYY")]
    (->> s
        (f/parse ->f)
        (f/unparse <-f))))

(defn send-git-query []
  (c/parse-string
   (:body (http/post "https://api.github.com/graphql"
                     {:headers {"Authorization" (str "bearer " (get-in config/env [:secrets :github]))}
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
    [:div.date [:div.display [:span.fas.fa-upload] "  " (format-repo-date pushedAt)]]])


(defn medium-feed []
  ;; articles and replies are currently in the same feed
  ;; so we try and differentiate them by checking if they
  ;; have categories associated with them :sadface
  (filter
   #(not (empty? (:categories %)))
   (:entries (feed/parse-feed (feed/uri-stream "https://medium.com/feed/@lilactown")))))

(defn article-category [{:keys [name]}]
  [:li.category name])

(defn format-article-date [d]
  (f/unparse (f/formatter "MMM YYYY") (clj-time.coerce/from-date d)))

(defn article [{:keys [title link published-date categories]}]
  [:li.article
   [:div.date (format-article-date published-date)]
   [:div.title
    [:div
     [:span [:a {:href link :target "_blank"} title]]]
    [:ul.categories (map article-category categories)]]])

(defn fetch []
  (let [data-ch (async/chan)]
    (async/go (async/>! data-ch {:github (send-git-query)}))
    (async/go (async/>! data-ch {:medium (medium-feed)}))
    (loop [data []]
      (let [data' (conj data (async/<!! data-ch))]
        (if (= 2 (count data'))
          (do (async/close! data-ch)
              (apply merge data'))
          (recur data'))))
    ))

(defn fetch' [& reqs]
  (->> reqs
       (map (fn [[tag do-req]]
              (async/thread {tag (do-req)})))
       (async/merge)
       (async/reduce merge {})
       (async/<!!)))

(defn html [bgcolor]
  (let [{github :github
         articles :medium} (fetch' [:github send-git-query]
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
       (garden/css (styles "#DCD0FF" ;; "#C8A2C8"
                    ))]]
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
         (map article articles)]]
       ]]]))
