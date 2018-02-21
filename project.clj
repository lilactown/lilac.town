(defproject lilactown "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.9.0"]
                 ;; feedparser deps
                 ;; [enlive "1.1.6"]
                 ;; [org.apache.httpcomponents/httpclient "4.5.5"]
                 ;; [com.rometools/rome "1.9.0"]
                 ;; lilactown deps
                 [hiccup "1.0.5"]
                 [ring/ring-core "1.6.3"]
                 [ring/ring-jetty-adapter "1.6.3"]
                 [mount "0.1.12"]
                 [garden "1.3.3"]
                 [clj-http "3.7.0"]
                 [vincit/venia "0.2.5"]
                 [cheshire "5.8.0"]
                 [clj-time "0.14.2"]
                 [org.clojure/core.async "0.4.474"]
                 [tick "0.3.5"]
                 [thheller/shadow-cljs "2.1.22"]]
  ;; :repl-options {:nrepl-middleware
  ;;                [shadow.cljs.devtools.server.nrepl/cljs-load-file
  ;;                 shadow.cljs.devtools.server.nrepl/cljs-eval
  ;;                 shadow.cljs.devtools.server.nrepl/cljs-select
  ;;                 ;; required by some tools, not by shadow-cljs.
  ;;                 cemerick.piggieback/wrap-cljs-repl]}
  :min-lein-version "2.0.0"
  :main ^:skip-aot lilactown.core
  :target-path "target/%s"
  :uberjar-name "lilactown.jar"
  :profiles {:uberjar {:aot :all}
             :client {:dependencies [[binaryage/devtools "0.9.9"]
                                     [com.cemerick/piggieback "0.2.2"] 
                                     [org.clojure/tools.nrepl "0.2.13"]
                                     [cider/cider-nrepl "0.16.0-SNAPSHOT"]
                                     [refactor-nrepl "2.4.0-SNAPSHOT"]]}})
