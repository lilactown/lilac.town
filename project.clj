(defproject lilactown "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.9.0"]
                 ;; shadow-cljs
                 ;; [org.clojure/clojurescript "1.10.339"]
                 [org.clojure/tools.reader "1.3.0"]
                ;; lilactown deps
                 [hiccup "1.0.5"]
                 [ring/ring-core "1.6.3"]
                 [ring/ring-jetty-adapter "1.6.3"]
                 [mount "0.1.12"]
                 [garden "1.3.3"]
                 [clj-http "3.7.0"]
                 [cheshire "5.8.0"]
                 [clj-time "0.14.2"]
                 [org.clojure/core.async "0.4.474"]
                 [tick "0.3.5"]
                 ]
  :min-lein-version "2.0.0"
  :main ^:skip-aot lilactown.core
  :target-path "target/%s"
  :uberjar-name "lilactown.jar"
  :profiles {:uberjar {:aot :all}
             :dev {:dependencies [[thheller/shadow-cljs "2.4.24"]
                                  [binaryage/devtools "0.9.10"]]
                   :source-paths ["src" "dev"]
                   :repl-options {:init-ns user
                                  :init (start!)}}})
