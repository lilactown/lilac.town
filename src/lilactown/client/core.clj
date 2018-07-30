(ns lilactown.client.core)

(defn module [{:keys [module init data ref]}]
  [:script {:type "lilactown/module"
            :data-module (name module)
            :data-init init
            :data-ref ref}
   (prn-str data)])

(defn main []
  [:script#mainjs {:src "assets/js/main.js" :async true
                   :type "text/javascript"}])
