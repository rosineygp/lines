(def! or
  (fn* [& more]
       (let* [c (count more)
              o (first more)]
             (if (<= c 1) o (if o o (apply or (rest more)))))))

(def! and
  (fn* [& more]
       (let* [c (count more)
              o (first more)]
             (if (<= c 1) o (if o (apply and (rest more)) o)))))

(def! hashmap-list
  (fn* [l]
       (map (fn* [k] (vector k (get l k))) (keys l))))

(def! merge
  (fn* [a b]
       (reduce (fn* [x y] (assoc x (first y) (last y))) a (hashmap-list b))))

(def! key-name
  (fn* [k]
       (if (keyword? k) (str-subs k 1) k)))

; return function from string
(def! call
  (fn* [f]
       (eval (symbol f))))

; check if object exist and is a function
(def! callable?
  (fn* [f]
       (fn?
        (try* (call f) (catch* ex)))))

(def! get-in
  (fn* [k v]
       (let* [r (get k (first v))]
             (cond
               (nil? r) nil
               (map? r) (get-in r (rest v))
               (keyword? :else) r))))

(def! even?
  (fn* [n]
       (if (= (mod n 2) 0) true false)))

(def! odd?
  (fn* [n]
       (if (= (even? n) true) false true)))

(def! load-once-mem! (atom []))

(def! load-once
  (fn* [f]
       (let* [loaded? (filter (fn* [a] (if (= a f) true false)) @load-once-mem!)]
             (if (empty? loaded?) (do
                                    (load-file-without-hashbang f)
                                    (swap! load-once-mem! concat [f]))))))

(def! key
  (fn* [k]
       (first (keys k))))

(def! val
  (fn* [k]
       (first (vals k))))

(def! to-list
  (fn* [v]
       (if (sequential? v) v (list v))))

(def! str-split-key-val
  (fn* [s]
       (let* [s (str-split s "=")]
             (hash-map (keyword (nth s 0)) (nth s 1)))))

(def! str-split-keys-values
  (fn* [s c]
       (reduce merge {} (map str-split-key-val (str-split s c)))))

(def! spit
  (fn* [f content & options]
       (if (empty? options)
         (file-write f content)
         (file-write f content (get (first options) :append)))))