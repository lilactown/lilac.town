(ns lilactown.site.slack
  (:require [ring.util.response :as res]
            [ring.util.request :as req]
            [hiccup.core :as h]
            [clj-time.core :as t]
            [clj-time.format :as f]
            [clj-time.coerce :as c]))

(defonce !logs (atom '()))

(defmulti slack-event :type)

(defmethod slack-event "url_verification"
  [body]
  (-> (:challenge body)
      (res/response)))

(defmethod slack-event "message"
  [event]
  (res/response "OK"))

(defn api [request]
  (let [body (:body-params request)]
    ;; ignore bot messages
    (if (not= (get-in body [:event :subtype]) "bot_message")
      (do (swap! !logs conj body)
          (if (:event body)
            (slack-event (:event body))
            (slack-event body)))
      (res/response "OK"))))

(defn logs [request]
  (-> @!logs
      (res/response)))

(def channels {"G8QAEEPEX" "delivery-team"
               "CBWR8FNQ4" "release-coordination"
               "C8KTKM3A5" "solution-discussion"
               "C87E0RZ6Y" "general"
               "C9Z02UF0T" "troubleshooting"
               "C8YEYGMFH" "mr"
               ;; dms
               "D14KSACM6" "Paul"
               "D8NLAJ7T8" "Evan"
               "D963EKUDS" "Uma"
               "D7FJRUU11" "Andy"
               "D5TUXUR96" "Dave"})

(def users {"W7MB482ER" "Uma"
            "W8Q8AE1AP" "Mamata"
            "W7M4SJN75" "Andy"
            "W7M70QHBL" "Mitch"
            "W7N9Q2QBH" "Dan"
            "W7LM73KGR" "Vye"
            "W7N9J95T9" "Paul"
            "W7MB47YFP" "Evan"
            "W7LHEFP3J" "Anita"
            "W7LM6P673" "Dave"
            "W7LM708DP" "Will"})

(defn format-time [ts]
  (try (-> ts
           (clojure.string/split #"\.")
           (first)
           (Integer.)
           (c/from-epoch)
           (as-> t
               (-> (f/formatter "E H:mm a")
                   (f/with-zone (t/time-zone-for-id "America/Los_Angeles"))
                   (f/unparse t))))
       (catch Exception _
         nil)))

(defn message-ui [messages]
(h/html
 [:html
  [:meta {:name "viewport"
          :content "width=device-width, initial-scale=1"}]
  [:body {:style "font-family: sans-serif"}
   (for [{:keys [channel user text ts channel_type]} messages]
     [:div {:style "border: 1px solid #3b3b3b; padding: 10px; margin: 5px"}
      [:div {:style (case channel_type
                      "im" "color: red"
                      "mpim" "color: mediumaquamarine"
                      "")}
       "[ " [:strong (get channels channel channel) " / " (get users user user)] " ] "
       (when ts (format-time ts))]
      [:div text]])]]))

(defn messages [request]
(let [channel (get (:query-params request) "chan")
      no-channel (get (:query-params request) "nochan")]
  (println channel)
  (->> @!logs
       (filter (fn [log] (= (get-in log [:event :type]) "message")))
       (filter (fn [log] (if (nil? channel) true
                             (let [ch-id (get-in log [:event :channel])]
                               (= channel
                                  (get channels ch-id ch-id))))))
       (filter (fn [log] (if (nil? no-channel) true
                             (let [ch-id (get-in log [:event :channel])]
                               (not= no-channel
                                     (get channels ch-id ch-id))))))
       (map :event)
       (message-ui)
       (res/response))))
