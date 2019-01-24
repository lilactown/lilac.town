(ns lilactown.site.writing
  (:require [clj-http.client :as http]
            [clojure.tools.reader.edn :as edn]
            [mount.core :as mount :refer [defstate]]
            [garden.core :as garden]
            [garden.stylesheet :refer [at-media]]
            [garden.units :refer [px]]
            [clj-time.format :as f]
            [clj-time.coerce :as coerce]
            [tick.core]
            [tick.timeline]
            [tick.clock]
            [tick.schedule]
            [clj-org.org :as org]))

(defn fetch-all-posts []
  (-> (http/get "https://api.lilac.town/writing/prod/list"
                {:headers {"Authorization" "allowed"}})
      :body
      (edn/read-string)))

(defn fetch-post-by-slug [slug]
  (-> (http/get (str "https://api.lilac.town/writing/prod/slug/" slug)
                {:headers {"Authorization" "allowed"}})
      :body
      (edn/read-string)))

(defstate posts
  :start (agent (fetch-all-posts)))

(defn update-posts! []
  (send-off posts #(do % (fetch-all-posts))))

(defn poll-timeline []
  (tick.timeline/timeline
   (tick.timeline/periodic-seq (tick.clock/now)
                               (tick.core/minutes 1))))

(defn schedule-poll [timeline]
  (tick.schedule/schedule #(do (update-posts!)
                               (println %))
                          timeline))

(defn start-polling [schedule]
  (tick.schedule/start schedule (tick.clock/clock-ticking-in-seconds)))

(defstate post-polling
  :start (let [scheduler (-> (poll-timeline)
                             (schedule-poll))]
           (start-polling scheduler)
           scheduler)
  :stop (tick.schedule/stop post-polling))

(def styles
  [[:* {:box-sizing "border-box"}]
   [:body {:font-family "'Roboto Condensed', sans-serif"
           :background-color "#fbfbfb"
           :color "#3b3b3b"}]
   [:h1 {:font-family "'Roboto Slab', serif"}
    [:small {:font-size "0.7em"
             :display "block"
             :color "#9a549a"
             :margin "-10px 0 0 90px"}]]
   [:a {:color "#371940"
        :text-decoration "none"}
    [:&:hover {:color "#9a549a"}]]
   [:a.title {:text-decoration "none"}]
   [:#main {:max-width "670px"
            :margin "40px auto 20px"}]
   [:.article {:padding "5px 5px"
               :display "flex"}
    [:.title {:font-size "1.5rem"
              :flex 1}]
    [:.author {:font-size "1rem"}]
    [:.date {:margin-right "12px"
             :font-size "1.2rem"
             :vertical-align "unset"
             :display "inline-block"
             :width "3rem"
             :border-right "1px solid #3b3b3b"}]]])

(defn post-date [d]
  (f/unparse (f/formatter "MMM YYYY") (coerce/from-date d)))

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
      [:a.title {:href "/"}
       [:h1.title "lilac.town"
        [:small "Writing"]]]]
     [:div
      (for [post (->> @posts :content
                      (filter #(contains? % :writing.content/published-at)))]
        [:a {:href (str "/writing/" (:writing.content/slug post))}
         [:div.article
          [:div.date (post-date (:writing.content/published-at post))]
          [:div.title (:writing.content/title post)
           [:div.author "By " (-> post
                                  :writing.content/author
                                  :writing.author/name)]]]])]]
    [:link {:href "https://use.fontawesome.com/releases/v5.0.6/css/all.css"
            :rel "stylesheet"}]
    [:link {:href "https://fonts.googleapis.com/css?family=Roboto+Condensed|Roboto+Slab"
            :rel "stylesheet"}]
    [:style (garden/css styles)]]])

(def post-styles
  [[:* {:box-sizing "border-box"}]
   [:body {:font-family "'Roboto Condensed', sans-serif"
           :background-color "#fbfbfb"
           :color "#3b3b3b"}]
   [:h1 {:font-family "'Roboto Slab', serif"}
    [:small {:font-size "0.7em"
             :display "block"
             :color "#9a549a"
             :margin "-10px 0 0 90px"}]]
   [:a {:color "#371940"}
    [:&:hover {:color "#9a549a"}]]
   [:a.title {:text-decoration "none"}]
   [:#main {:max-width "670px"
            :margin "40px auto 20px"}]
   [:article
    [:.title {:font-size "1.8em"
              :flex 1}]]])

(defn render-post [slug]
  (let [post (fetch-post-by-slug slug)
        body (:content (org/parse-org (:writing.content/body post)))]
    (cognitect.rebl/inspect (org/parse-org (:writing.content/body post)))
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
        [:a.title {:href "/"}
         [:h1.title "lilac.town"
          [:small "Writing"]]]]
       [:article
        [:h1.title (:writing.content/title post)]
        [:div {:style "text-align: right;"}
         [:div [:small "Published " (:writing.content/published-at post)]]
         [:div [:small "Last updated " (:writing.content/edited-at post)]]]
        body]]
      [:link {:rel "stylesheet"
              :href "//cdnjs.cloudflare.com/ajax/libs/highlight.js/9.13.1/styles/default.min.css"}]
      [:script {:src "//cdnjs.cloudflare.com/ajax/libs/highlight.js/9.13.1/highlight.min.js"}]
      [:script {:src "https://cdnjs.cloudflare.com/ajax/libs/highlight.js/9.13.1/languages/clojure.min.js"}]
      [:script "hljs.initHighlightingOnLoad()"]
      [:link {:href "https://use.fontawesome.com/releases/v5.0.6/css/all.css"
              :rel "stylesheet"}]
      [:link {:href "https://fonts.googleapis.com/css?family=Roboto+Condensed|Roboto+Slab"
              :rel "stylesheet"}]
      [:style (garden/css post-styles)]]]))