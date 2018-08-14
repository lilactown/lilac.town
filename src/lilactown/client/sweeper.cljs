(ns lilactown.client.sweeper
  (:require [lilactown.react :as r]
            [lilactown.react.dom :as dom]
            [lilactown.css :as css]))

;; State

(defn initial-state [size mines]
  ;; convert into a map so we can navigate the grid cheaply
  (into
   {}
   (for [[i square] (-> (- (* size size) mines) ;; size of grid minus the number of mines
                        (repeat {:explodes? false}) ;; map them to squares that don't explode
                        (into (repeat mines {:explodes? true})) ;; add squares that do explode
                        (shuffle) ;; shuffle all of them so that they're in random order
                        ;; associate the index with each square [i square]
                        (as-> l (map-indexed vector l)))]
     ;; we add 1 since CSS grid wants 1..n
     (let [row (inc (quot i size))
           col (inc (mod i size))]
       [[row col] (assoc square
                         :marked? false)]))))

(def grid-state (atom (initial-state 15 80)))

;; Styles

(def square-style
  (css/edn
   {:width "30px"
    :height "30px"
    :background-color "rgb(187, 164, 255)"
    :color "white"
    :font-family "sans-serif"
    :display "flex"
    :justify-content "center"
    :align-items "center"
    :box-shadow "2px 2px 3px rgba(100, 100, 100, .5)"}))

(def hover-buzz-animation
  (css/keyframes
   "50% {
      transform: translateX(3px) rotate(2deg)
   }
   100% {
     transform: translateX(-3px) rotate(-2deg)
   }"))

(def hover-buzz-style
  (css/edn
   {"&:hover, &:focus, &:active"
    {:animation-name hover-buzz-animation
     :animation-duration "0.15s"
     :animation-timing-function "linear"
     :animation-iteration-count "infinite"}}))


;; Components

(r/defnc Square
  ;; {:watch (fn [_] {:hover? (atom false)})}
  [{:keys [col row explodes? marked?]} ;; {:keys [hover?]}
   ]
  (dom/div {:style #js {:gridColumn col
                        :gridRow row
                        :backgroundColor (when explodes? "black")}
            :className (str square-style " " hover-buzz-style)}
           (when marked? "?")))

(r/defnc Grid [{state :state}]
  (dom/div
   (dom/div {:style #js {:display "grid"
                         :gridGap "5px"}}
            ;; keys are [col row], value is square state
            (for [[[col row] square] state]
              (Square (assoc square
                             :key [col row]
                             :col col
                             :row row))))))


;; Hook up to state

(r/defrc Container
  {:watch (fn [_] {:state grid-state})}
  [_ {state :state}]
  (Grid {:state @state}))
