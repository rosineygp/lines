(defn or [& i]
  (let [n (map (fn [a] (if (= a true) 1 0)) i)
        r (reduce (fn [a b] (+ a b)) 0 n)]
    (if (> r 0) true false)))

(defn and [& i]
  (let [n (map (fn [a] (if (= a true) 1 0)) i)
        r (reduce (fn [a b] (+ a b)) 0 n)]
    (if (= r (count n)) true false)))

(defn hashmap-list [l]
  (map (fn [k] (vector k (get l k))) (keys l)))

(defn key-name [k]
  (if (keyword? k) (str-subs k 1) nil))

; return function from string
(defn call [f]
  (eval (symbol f)))

; check if object exist and is a function
(defn callable? [f]
  (fn?
   (try* (call f) (catch* ex))))

(defn key-exist? [m k]
  (not (nil? (get m k))))