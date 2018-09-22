(ns lilactown.client.visual-spec
  (:require [lilactown.react :as r]
            [lilactown.react.dom :as dom]
            ["react-dom" :as react-dom]
            [cljs.js :as cljs]
            [cljs.env :as env]
            [shadow.cljs.bootstrap.browser :as boot]
            ["react-codemirror" :as react-cm]
            ["codemirror/mode/clojure/clojure.js"]
            ))

(defonce vs-env (env/default-compiler-env))

(defonce !results (atom nil))

(defn print-results! [results]
  (reset! !results results))

(defonce !code (atom "int?"))

(defn update-code! [s]
  (reset! !code s))

(defonce !data (atom "\"foo\""))

(defn update-data! [e]
  (reset! !data e))


(defn compile-it [code data]
  (cljs/eval-str
   vs-env
   (str "(require '[clojure.spec.alpha :as s])
(require '[expound.alpha :as expound])
(s/def :visual-spec/user-spec
 (do " code "))
(def data " data ")
(expound/expound-str :visual-spec/user-spec data)")
   'visual-spec.eval-test
   {:eval cljs/js-eval
    :load (partial boot/load vs-env)}
   print-results!))


(def CodeMirror (r/factory react-cm))

(r/defrc Editor
  {:watch (fn [_] {:results !results
                   :code !code
                   :data !data})
   :componentWillMount
   (fn [_]
     (boot/init vs-env
                {:path "/assets/bootstrap"}
                #(println "Bootstrapper init")))}
  [_ {:keys [results code data]}]
  (dom/div
   (dom/div
    {:style #js {:display "flex"}}
    (dom/div
     {:style #js {:padding "5px"
                  :flex 1}}
     (CodeMirror {:value @code
                  :onChange update-code!
                  :options #js {:mode "clojure"}}))
    (dom/div
     {:style #js {:padding "5px"
                  :flex 1}}
     (CodeMirror {:value @data
                  :onChange update-data!
                  :options #js {:mode "clojure"}})))
   (dom/div
    (dom/button
     {:onClick #(compile-it @code @data)}
     "Run"))
   (dom/pre
    {:dangerouslySetInnerHTML
     #js {:__html (:value @results)}})
   ))

(defn ^:export start! [node]
  (println "Starting editor")
  (react-dom/render (Editor) node)
  )
