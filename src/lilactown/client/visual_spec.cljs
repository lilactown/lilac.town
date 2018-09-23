(ns lilactown.client.visual-spec
  (:require [lilactown.react :as r]
            [lilactown.react.dom :as dom]
            ["react-dom" :as react-dom]
            [cljs.js :as cljs]
            [cljs.env :as env]
            [cljs.tools.reader :as reader]
            [taoensso.timbre :as t]
            [expound.alpha :as expound]
            [shadow.cljs.bootstrap.browser :as boot]
            ["react-codemirror" :as react-cm]
            ["codemirror/mode/clojure/clojure.js"]
            ["codemirror/keymap/vim.js"]
            ["parinfer-codemirror" :as parinfer]
            [clojure.string :as str]))

(defonce vs-env (env/default-compiler-env))

(defonce !state (atom {:results nil
                       :code "(s/def ::foo int?)
(s/keys :req-un [::foo])"
                       :data "{:foo \"foo\"}"
                       :analysis nil
                       :data-highlight nil}))

(defonce !cm-data (atom nil))

(defonce !cm-code (atom nil))

(defonce !data-markers (atom []))


(defn mark-data-error! [line from to]
  (when @!cm-data
    (let [doc ^js (. @!cm-data getCodeMirror)]
      (swap! !data-markers conj
             (. ^js doc markText
                #js {:line line :ch from}
                #js {:line line :ch to}
                #js {:className "syntax-error"
                     :css "background-color: rgba(255, 0, 0, .3)"})))))

(defn clear-data-markers! []
  (when @!cm-data
    (doseq [marker @!data-markers]
      (.clear marker))))


(defn update-code! [s]
  (swap! !state assoc :code s))

(defn update-data! [e]
  (swap! !state assoc :data e))



(defn line-range [analysis path]
  (let [full-path-val (get-in analysis (into [:value :form] path))
        meta-info (meta full-path-val)]
    (if (nil? meta-info)
      ;; path points to a scalar, get meta on coll above it
      (meta (get-in analysis (into [:value :form] (drop-last path))))
      meta-info)))

#_(line-range (:analysis @!state) (:code @!state) [:foo])

(defn line+index [{:keys [line end-line]} data path val]
  (t/debug line end-line)
  (-> data
      (str/split-lines)
      ;; get lines between line and end-line
      (subvec (dec line) end-line)
      (as-> lines
          ;; return [index line] in vector when string included
          (keep-indexed #(when (str/includes? %2 (pr-str val))
                           [(+ %1 line -1) %2]) lines)
        (first lines))))

(defn index+position [[index line] val]
  [index
   (str/index-of line (pr-str val))
   (+ (str/index-of line (pr-str val))
      (count (pr-str val)))])



(defn compile-it [code data]
  (clear-data-markers!)
  (cljs/eval-str
   vs-env
   (str "(require '[clojure.spec.alpha :as s])
(require '[expound.alpha :as expound])
(def code (s/spec (do " code ")))
(when (not (s/spec? code))
  (throw (js/Error. \"Code must return a spec\")))
(s/def :visual-spec/user-spec
 code)
(def data " data ")
{:expound
  (expound/expound-str :visual-spec/user-spec data)
 :data (s/explain-data :visual-spec/user-spec data)}")
   'visual-spec.eval-test
   {:eval cljs/js-eval
    :load (partial boot/load vs-env)}
   (fn [results]
     (cljs.js/analyze-str
      (cljs.js/empty-state) data nil {}
      (fn [analysis]
        (let [spec-problems (get-in results
                                    [:value :data :cljs.spec.alpha/problems])]
        (swap! !state assoc :results results :analysis analysis)
        (when spec-problems
          (doseq [{:keys [path val] :as problem} spec-problems]
            (let [[line from to] (-> analysis
                                     (line-range path)
                                     (line+index data path val)
                                     (index+position val))]
              (t/debug spec-problems)
              (mark-data-error! line from to))))))))))

(def CodeMirror (r/factory react-cm))



(r/defreactive Editor
  :watch (fn [_] {:state !state})
  :componentWillMount
  (fn [_]
    (boot/init vs-env
               {:path "/assets/bootstrap"}
               #(t/debug "Bootstrapped")))

  :componentDidMount
  (fn [this]
    (. parinfer init ^js (. @!cm-code getCodeMirror))
    (. parinfer init ^js (. @!cm-data getCodeMirror)))
  :render
  (fn [this {:keys [state]}]
    (let [{:keys [code results data]} @state]
      (dom/div
       (dom/div
        {:style #js {:display "flex"}}
        (dom/div
         {:style #js {:padding "5px"
                      :flex 1}}
         (CodeMirror {:value code
                      :ref #(reset! !cm-code %)
                      :onChange update-code!
                      :options #js {:mode "clojure"
                                    ;; :keyMap "vim"
                                    }}))
        (dom/div
         {:style #js {:padding "5px"
                      :flex 1}}
         (CodeMirror {:value data
                      :ref #(reset! !cm-data %)
                      :onChange update-data!
                      :options #js {:mode "clojure"
                                    ;; :keyMap "vim"
                                    }})))
       (dom/div
        (dom/button
         {:onClick #(compile-it code data)}
         "Run"))
       (dom/pre
        (or (get-in results [:value :expound])
            (prn-str (:error results))))))))

(defn ^:export start! [node]
  (println "Starting editor")
  (react-dom/render (Editor) node))
