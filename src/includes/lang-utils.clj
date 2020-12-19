(defn or [& more]
  (let [c (count more)
        o (first more)]
    (if (<= c 1) o (if o o (apply or (rest more))))))

(defn and [& more]
  (let [c (count more)
        o (first more)]
    (if (<= c 1) o (if o (apply and (rest more)) o))))

(defn hashmap-list [l]
  (map (fn [k] (vector k (get l k))) (keys l)))

(defn merge [a b]
  (reduce (fn [x y] (assoc x (first y) (last y))) a (hashmap-list b)))

(defn key-name [k]
  (if (keyword? k) (str-subs k 1) k))

; return function from string
(defn call [f]
  (eval (symbol f)))

; check if object exist and is a function
(defn callable? [f]
  (fn?
   (try* (call f) (catch* ex))))

(defn get-in [k v]
  (let [r (get k (first v))]
    (cond
      (nil? r) nil
      (map? r) (get-in r (rest v))
      (keyword? :else) r)))

(defn even? [n]
  (if (= (mod n 2) 0) true false))

(defn odd? [n]
  (if (= (even? n) true) false true))

(def load-once-mem! (atom []))

(defn load-once [f]
  (let [loaded? (filter (fn [a] (if (= a f) true false)) @load-once-mem!)]
    (if (empty? loaded?) (do
                           (load-file-without-hashbang f)
                           (swap! load-once-mem! concat [f])))))

(defn key [k]
  (first (keys k)))

(defn val [k]
  (first (vals k)))

(defn to-list [v]
  (if (sequential? v) v (list v)))

(defn str-split-key-val [s]
  (let [s (str-split s "=")]
    (hash-map (keyword (nth s 0)) (nth s 1))))

(defn spit [f content & options]
  (if (empty? options)
    (file-write f content)
    (file-write f content (get (first options) :append))))