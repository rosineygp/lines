(defn or [& i]
  (let [n (map (fn [a] (if (= a true) 1 0)) i)
        r (reduce (fn [a b] (+ a b)) 0 n)]
    (if (> r 0) true false)))

(defn hashmap-list [l]
  (map (fn [k] (vector k (get l k))) (keys l)))

(defn key-name [k]
  (if (keyword? k) (str-subs k 1) nil))