(defn fib* [a b n]
  (if (> n 0)
    (fib* b (+ a b) (- n 1))
    a))

(def fib (partial fib* 0 1))

(def numbers [1 2 3 4 5 6 7 8 9 10 11 12 13 14 15 16 17 18 19 20 21 22 23 24 25 26 27 28 29 30
           31 32 33 34 35 36 37 38 39 40 41 42 43 44 45 46 47 48 49 50 51 52 53 54 55 56 57 58 59 60])

(def t (time-ms))

(println (pmap (fn [x]
                 (fib x)) numbers))

(println "\n\n" "pmap" (- (time-ms) t) "ms")

(def strings ["text" "text" "text" "rosiney\ngomes rosiney" "text" "text" "text" "text" "text" "text" "text" "text" "text"])

(println (pmap (fn [x]
                 (str-upper-case x)) strings))

(println (pmap (fn [x]
                 (> x 15)) numbers))

(println (pmap (fn [x]
                 (println x)) numbers))

(println (pmap (fn [x]
                 {:teste x}) numbers))

(println (pmap (fn [x]
                 (vector 1 x)) numbers))


(def t (time-ms))

(println (map (fn [x]
  (fib x)) numbers))

(println "\n\n" "map" (- (time-ms) t) "ms")