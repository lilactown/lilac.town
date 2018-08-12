(ns lilactown.data
  (:require [clojure.spec.alpha :as s]
            [clojure.spec.test.alpha :as stest]))

(defn project-paths [projection]
  (loop [pr projection
         paths '[]]
    (let [[el next] pr]
      (if (empty? pr)
        paths

        (if (vector? next)
          (recur
           (drop 2 pr)
           (concat paths (map #(apply vector el %) (project-paths next))))

          (recur
           (rest pr)
           (conj paths [el])))))))

(s/def ::projection
  (s/+ (s/alt :key keyword?
              :sym symbol?
              :projection (s/spec ::projection))))

#_(s/explain ::projection [:a :b])

#_(s/valid? ::projection [:a [:b]])

#_(s/valid? ::projection [:a [1 'a] :b])

(s/fdef project-paths
  :args (s/cat :projection
               ::projection))

#_(assert (= (project-paths [:a [:b1 [:c1 [:d1 :a2]]] :b [:c :d]
                             'a 'b 'c])
             (list ['c] ['b] ['a] [:a :b1 :c1 :d1] [:a :b1 :c1 :a2] [:b :c] [:b :d])))


#_(def m {:a {:b "foo" :c {:d "1234"}} :x "456" :y 1 :z 2})

#_(assert (= (reduce (fn [m' p]
                       (assoc-in m' p (get-in m p)))
                     {}
                     (project-paths [:a [:b] :y]))
             {:y 1, :a {:b "foo"}}))

(defn project [m projection]
  (reduce (fn [m' p]
            (assoc-in m' p (get-in m p)))
          {}
          (project-paths [:a [:b] :y])))

(stest/instrument `project-paths)
