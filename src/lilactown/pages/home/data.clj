(ns lilactown.pages.home.data
  (:require [lilactown.config :as config]
            [clj-http.client :as http]
            [cheshire.core :as c]
            [clojure.string :as s]
            ;; [feedparser-clj.core :as feed]
            [clojure.core.async :as async]
            [clojure.java.io :as io]))

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

(defn git []
  (c/parse-string
   (:body (http/post "https://api.github.com/graphql"
                     {:headers {"Authorization" (str "bearer " (get-in config/env [:secrets :github]))}
                      :body (str "{\"query\": \"" query "\"}")}))
   true))

;; (defn medium-feed []
;;   ;; articles and replies are currently in the same feed
;;   ;; so we try and differentiate them by checking if they
;;   ;; have categories associated with them :sadface
;;   (filter
;;    #(not (empty? (:categories %)))
;;    (:entries (feed/parse-feed (feed/uri-stream "https://medium.com/feed/@lilactown")))))



(defn medium []
  (->> (-> "https://medium.com/@lilactown/latest?format=json"
           (http/get)
           (:body)
           (clojure.string/replace-first "])}while(1);</x>" "")
           (c/parse-string)
           (get-in ["payload" "references" "Post"]))
       (map second)
       (map #(let [created-at (get % "createdAt")
                   title (get % "title")
                   tags (get-in % ["virtuals" "tags"])
                   claps (get-in % ["virtuals" "totalClapCount"])
                   slug (get % "uniqueSlug")]
               {:created-at created-at
                :title title
                :tags (map (fn [t] (get t "name")) tags)
                :claps claps
                :link (str "https://medium.com/@lilactown/" slug)}))))


;; (defn fetch []
;;   (let [data-ch (async/chan)]
;;     (async/go (async/>! data-ch {:github (send-git-query)}))
;;     (async/go (async/>! data-ch {:medium (medium-feed)}))
;;     (loop [data []]
;;       (let [data' (conj data (async/<!! data-ch))]
;;         (if (= 2 (count data'))
;;           (do (async/close! data-ch)
;;               (apply merge data'))
;;           (recur data'))))
;;     ))

(defn fetch [& reqs]
  (->> reqs
       (map (fn [[tag do-req]]
              (async/thread {tag (do-req)})))
       (async/merge)
       (async/reduce merge {})
       (async/<!!)))
