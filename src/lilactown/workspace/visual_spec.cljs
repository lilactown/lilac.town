(ns lilactown.workspace.visual-spec
  (:require [lilactown.client.visual-spec :as visual-spec]
            [lilactown.react :as r]
            [lilactown.react.dom :as dom]
            [cljs.js :as cljs]
            [cljs.env :as env]
            [shadow.cljs.bootstrap.browser :as boot]
            [nubank.workspaces.core :as ws]
            [nubank.workspaces.card-types.react :as ct.react]))

(defonce vs-env (env/default-compiler-env))

(defonce !results (atom nil))

(defn print-results! [results]
  (reset! !results results))

(defonce !code (atom ""))

(defn update-code! [e]
  (reset! !code (.. e -target -value)))

(defonce !data (atom ""))

(defn update-data! [e]
  (reset! !data (.. e -target -value)))


(defn compile-it [code data]
  (cljs/eval-str
   vs-env
   (str "(require '[clojure.spec.alpha :as s])
(s/def :visual-spec/user-spec
 (do " code "))
(def data " data ")
(s/explain-data :visual-spec/user-spec data)")
   'visual-spec.eval-test
   {:eval cljs/js-eval
    :load (partial boot/load vs-env)}
   print-results!))

(r/defrc Editor
  {:watch (fn [_] {:results !results
                   :code !code
                   :data !data})
   :componentWillMount
   (fn [_]
     (boot/init vs-env
                {:path "/bootstrap"}
                #(println "Bootstrapper init")))}
  [_ {:keys [results code data]}]
  (dom/div
   (dom/div
    {:style #js {:display "flex"}}
    (dom/div
     {:style #js {:padding "5px"}}
     (dom/textarea {:style #js {:height "200px" :width "300px"}
                    :value @code
                    :onChange update-code!}))
    (dom/div
     {:style #js {:padding "5px"}}
     (dom/textarea {:style #js {:height "200px" :width "300px"}
                    :value @data
                    :onChange update-data!})))
   (dom/div
    (dom/button
     {:onClick #(compile-it @code @data)}
     "Run"))
   (dom/code
    (prn-str (:value @results)))))

(ws/defcard Editor
  (ct.react/react-card
   (Editor)))
