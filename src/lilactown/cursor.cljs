(ns lilactown.cursor
  (:require [clojure.zip]
            [clojure.walk :as w]))

(deftype ReadCursor [ref select meta]
  Object
  (equiv [this other]
    (-equiv this other))

  IEquiv
  (-equiv [this other]
    (identical? this other))

  IMeta
  (-meta [_] meta)

  IHash
  (-hash [this] (goog/getUid this))

  IDeref
  (-deref [_]
    (select (-deref ref)))

  IWatchable
  (-add-watch [this key run]
    (add-watch ref (list this key)
               (fn [_ _ old-v new-v]
                 (let [old (select old-v)
                       new (select new-v)]
                   (when (not= old new)
                     (run key this old new))))))
  (-remove-watch [this key]
    (remove-watch ref (list this key))
    this))

(defn select
  ([ref selector]
   (select ref selector nil))

  ([ref selector & {meta :meta}]
   (ReadCursor. ref selector meta)))
