(ns lilactown.pages.home.format
  (:require [clj-time.format :as f]
            [clj-time.coerce]))

(defn article-date [d]
  (f/unparse (f/formatter "MMM YYYY") (clj-time.coerce/from-date d)))

(defn repo-date [s]
  (let [->f (f/formatter (f/formatters :date-time-no-ms))
        <-f (f/formatter "MMM YYYY")]
    (->> s
         (f/parse ->f)
         (f/unparse <-f))))
