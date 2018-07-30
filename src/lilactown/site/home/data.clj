(ns lilactown.site.home.data
  (:require [lilactown.config :as config]
            [clj-http.client :as http]
            [cheshire.core :as c]
            [clojure.string :as s]
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

(defn github [token]
  (c/parse-string
   (:body (http/post "https://api.github.com/graphql"
                     {:headers {"Authorization" (str "bearer " token)}
                      :body (str "{\"query\": \"" query "\"}")}))
   true))

(defn parse-medium [data]
  (->> data
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

(defn medium []
  (->> (-> "https://medium.com/@lilactown/latest?format=json"
           (http/get)
           (:body)
           (clojure.string/replace-first "])}while(1);</x>" "")
           (c/parse-string)
           (get-in ["payload" "references" "Post"]))
       (parse-medium)))

(defn fetch [& reqs]
  (->> reqs
       (map (fn [[tag do-req]]
              (async/thread {tag (do-req)})))
       (async/merge)
       (async/reduce merge {})
       (async/<!!)))
