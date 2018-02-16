(ns feedparser-clj.core
  (:import (java.net URL)
           (com.rometools.rome.io SyndFeedInput XmlReader)
           (org.apache.http.impl.client HttpClients)
           (org.apache.http.client.methods HttpGet))
  (:require [net.cgrand.enlive-html :as html])
  (:gen-class))

(defrecord feed [authors categories contributors copyright description
                 encoding entries feed-type image language link entry-links
                 published-date title uri])
(defrecord entry [authors categories content contributors description
                  enclosures link published-date title updated-date url])
(defrecord enclosure [length type uri])
(defrecord person [email name uri])
(defrecord category [name taxonomyURI])
(defrecord content [type value])
(defrecord image [description link title url])
(defrecord link [href hreflang length rel title type])

(defn make-enclosure [e]
  (map->enclosure {:length (.getLength e) :type (.getType e)
                   :url (.getUrl e)}))

(defn make-content [c]
  (map->content {:type (.getType c) :value (.getValue c)}))

(defn text-content [c]
  (let [{:keys [type value]} (make-content c)]
    (if (not= "html" type) value
      (apply str (html/select (html/html-snippet value) [html/text-node])))))

(defn make-link [l]
  (map->link {:href (.getHref l) :hreflang (.getHreflang l)
              :length (.getLength l) :rel (.getRel l) :title (.getTitle l)
              :type (.getType l)}))

(defn make-category [c]
  (map->category {:name (.getName c)
                  :taxonomyURI (.getTaxonomyUri c)}))

(defn make-person [sp]
  (map->person {:email (.getEmail sp)
                :name (.getName sp)
                :uri (.getUri sp)}))

(defn make-image [i]
  (map->image {:description (.getDescription i)
               :link (.getLink i)
               :title (.getTitle i)
               :url (.getUrl i)}))

(defn make-entry [e]
  (map->entry {:authors (map make-person (seq (.getAuthors e)))
               :categories (map make-category (seq (.getCategories e)))
               :content (when-let [c (first (.getContents e))]
                          (make-content c))
               :contributors (map make-person (seq (.getContributors e)))
               :description (if-let [d (.getDescription e)] (make-content d))
               :enclosures (map make-enclosure (seq (.getEnclosures e)))
               :link (.getLink e)
               :published-date (.getPublishedDate e)
               :title (text-content (.getTitleEx e))
               :updated-date (.getUpdatedDate e)
               :uri (.getUri e)}))

(defn make-feed [f]
  (map->feed {:authors (map make-person (seq (.getAuthors f)))
              :categories (map make-category (seq (.getCategories f)))
              :contributors (map make-person (seq (.getContributors f)))
              :copyright (.getCopyright f)
              :description (.getDescription f)
              :encoding (.getEncoding f)
              :entries (map make-entry (seq (.getEntries f)))
              :feed-type (.getFeedType f)
              :image (if-let [i (.getImage f)] (make-image i))
              :language (.getLanguage f)
              :link (.getLink f)
              :entry-links (map make-link (seq (.getLinks f)))
              :published-date (.getPublishedDate f)
              :title (text-content (.getTitleEx f))
              :uri (.getUri f)}))

(defn uri-stream [uri]
  (let [client (-> (HttpClients/custom)
                   (.useSystemProperties)
                   (.disableCookieManagement)
                   (.build))
        response (.execute client (HttpGet. uri))]
    (.getContent (.getEntity response))))

(defn parse-feed [stream]
  (make-feed
    (.build (SyndFeedInput.) (XmlReader. stream))))
