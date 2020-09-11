(defn or [& i]
  (let [r (atom 0)]
    (do
      (map (fn [x]
             (if (= x true) (swap! r inc) false)) i)
      (if (> @r 0) true false))))

(defn hashmap-list [l]
  (map (fn [k] (vector k (get l k))) (keys l)))

(defn key-name [k]
  (if (keyword? k) (str-subs k 1) nil))