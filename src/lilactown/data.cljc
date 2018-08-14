(ns lilactown.data
  (:require [clojure.spec.alpha :as s]
            [clojure.spec.test.alpha :as stest]))

(defn project-paths
  "Takes a projection of a map and creates a vector of paths that match that
  projection. E.g.:

  (project-paths [:a
                  :b [:c]
                  :d [:e :f [:g]]])
  => [[:a]
      [:b :c]
      [:d :e]
      [:d :f :g]]"
  [projection]
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

(defn project
  "Takes a map `m` and a `projection` of that map, and returns a new map that is
   the image of the projection of `m` onto itself. E.g.:

   (project {:a {:b \"foo\"
                 :c {:d \"1234\"}}
             :x \"456\"
             :y 1
             :z 2}
            [:a [:b]
             :y])
   => {:a {:b \"foo\"}
       :y 1}"
  [m projection]
  (reduce (fn [m' p]
            (assoc-in m' p (get-in m p)))
          {}
          (project-paths [:a [:b] :y])))

#_(stest/instrument `project-paths)
