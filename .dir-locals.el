;;; Directory Local Variables
;;; For more information see (info "(emacs) Directory Variables")

((clojure-mode
  (cider-clojure-cli-global-options . "-A:server:dev")
  (cider-jack-in-nrepl-middlewares . ("nrebl.middleware/wrap-nrebl"
                                      "cider.nrepl/cider-middleware"))))
