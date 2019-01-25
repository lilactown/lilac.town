(ns lilactown.site.writing
  (:require [clj-http.client :as http]
            [clojure.tools.reader.edn :as edn]
            [mount.core :as mount :refer [defstate]]
            [garden.core :as garden]
            [garden.stylesheet :refer [at-media]]
            [garden.units :refer [px]]
            [clj-time.core :as t]
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
            :margin "40px auto 20px"}]])

(def post-list-styles
  [:#post-list
   [:.article {:padding "5px 5px"
               :display "flex"
               :font-size "1.2em"}
    [:.title {:font-size "1.5rem"
              :flex 1}]
    [:.author {:font-size "1rem"}]
    [:.date {:margin-right "12px"
             :font-size "1.2rem"
             :vertical-align "unset"
             :display "inline-block"
             :width "2.5rem"
             :border-right "1px solid #3b3b3b"}]]])

(defn post-date [d]
  (f/unparse (f/formatter "MMM DD") (coerce/from-date d)))

(defn post-list []
  [:div#post-list
   (for [post (->> @posts :content
                   (filter #(contains? % :writing.content/published-at))
                   (reverse))]
     [:a {:href (str "/writing/" (:writing.content/slug post))}
      [:div.article
       [:div.date (post-date (:writing.content/published-at post))]
       [:div.title (:writing.content/title post)
        [:div.author "By " (-> post
                               :writing.content/author
                               :writing.author/name)]]]])
   [:style (garden/css post-list-styles)]])

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
     (post-list)]
    [:link {:href "https://use.fontawesome.com/releases/v5.0.6/css/all.css"
            :rel "stylesheet"}]
    [:link {:href "https://fonts.googleapis.com/css?family=Roboto+Condensed|Roboto+Slab"
            :rel "stylesheet"}]
    [:style (garden/css styles)]]])

(def post-styles
  '[[:* {:box-sizing "border-box"}]
    [:body {:font-family "'Roboto Slab', serif"
            :background-color "#fbfbfb"
            :color "#3b3b3b"
            :padding-bottom "50px"}]
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
    [:article {:font-size "1.2em"}
     ;; [:.title {:font-size "1.8em"
     ;;           :flex 1}]
     [:small {:font-size ".7em"}]
     [:blockquote {:background "#f1f1ff"
                   :margin 0
                   :padding "1px 20px"}]
     [:h1 :h2 :h3
      {:font-family "'Roboto Condensed', sans-serif"}
      [:a {:text-decoration "none"}]]
     [:pre {:font-size "0.85em !important"}]
     [:code {:background "#f1f1ff"}]
     [:code.language-clojure {:background "inherit"}]]])

(defn pub-date [d]
  (f/unparse (f/formatter "MMMM DD, YYYY") d))

(defn updated-date [d]
  (f/unparse (f/formatter "MMMM DD, YYYY") d))

(defn render-post [slug]
  (let [post (fetch-post-by-slug slug)
        body (:content (org/parse-org (:writing.content/body post)))
        edited-at (coerce/from-date (:writing.content/edited-at post))
        published-at (coerce/from-date (:writing.content/published-at post))]
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
              :as "style"}]
      [:link {:href "/assets/prism-ghcolors.css"
              :rel "stylesheet"}]]
     [:body
      [:div#main
       [:div {:style "margin: 0 10px"}
        [:a.title {:href "/"}
         [:h1.title "lilac.town"
          [:small "Writing"]]]]
       [:article
        [:h1.title [:a {:id "top"}
                    (:writing.content/title post)]]
        [:div {:style "text-align: right;"}
         (when published-at
           [:div [:small "Published " (pub-date published-at)]])
         (when (and edited-at
                    published-at
                    (t/after? edited-at
                              published-at))
           [:div [:small "Last updated " (updated-date edited-at)]])]
        body]
       [:a {:href "#top"} "↑ Top"]]
      [:script {:src "/assets/prism.js"}]
      [:link {:href "https://use.fontawesome.com/releases/v5.0.6/css/all.css"
              :rel "stylesheet"}]
      [:link {:href "https://fonts.googleapis.com/css?family=Roboto+Condensed|Roboto+Slab"
              :rel "stylesheet"}]
      [:style (garden/css post-styles)]]]))
