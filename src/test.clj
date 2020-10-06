; super small framework for unit test

(def asserts (atom 0))
(def exit-code (atom 0))

; (defn deftest [name f]
;   (let [start (time-ms)]
;     (do
;       f
;       (if (= @exit-code 0)
;         (do
;           (println (str "\033[32m[ " name  " ]\033[0m"
;                    " asserts: " @asserts "/" (- @asserts @exit-code) "\n"))
;           (reset! asserts 0))
;         (do
;           (println (str "\033[31m[ " name  " ]\033[0m"
;                    " asserts: " @asserts "/" (- @asserts @exit-code) "\n"))
;           (exit! @exit-code))))))

; (defn testing [msg f]
;   (do
;     (swap! asserts inc)
;     (println (if f
;                (do
;                  (str "\033[32m[   ok   ]\033[0m"))
;                (do
;                  (swap! exit-code inc)
;                  (str "\033[31m[ failed ]\033[0m"))) msg)))

(defn is [comparison]
  (= comparison true))

(defn deftest [title & l]
  (let [total (count l)
        success (reduce (fn [a b] (+ a b)) 0 (map (fn [x] (if (= (get x :result) true) 1 0)) l))
        total-time (reduce (fn [a b] (+ a b)) 0 (map (fn [x] (get x :time-elapsed)) l))
        e (get (last l) :end)]
    (do
      (println "[" title "]" (str success "/" total) ":total-time" total-time "ms\n")
      (if (= success total) l (exit! 1)))))
  

(defmacro! testing
  (fn* [name body]
       `(let [start (time-ms)
              result ~body
              end (time-ms)
              time-elapsed (- end start)]
          (do
            (cond
              (= result true)  (println "\033[32m[   ok   ]\033[0m " ~name ":time" time-elapsed "ms")
              (= result false) (println "\033[31m[ failed ]\033[0m " ~name ":time" time-elapsed "ms" "\n  " '~body))
            (hash-map :name ~name :start start :result result :end end :time-elapsed time-elapsed)))))