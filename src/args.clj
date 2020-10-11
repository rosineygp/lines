(load-file-without-hashbang "src/includes/lang-utils.clj")

(defn help []
  (println "Usage: lines [OPTION]... [FILE]...
A pure bash clojureish CI pipeline.

Options:
-i, --inventory     inventory file.
-p, --pipeline      pipeline file.
-c, --clojure       for clj file (pure clojure pipeline)"))

(defn options [o]
  (cond
    (or (= o "-p") (= o "--pipeline")) (keyword "pipeline")
    (or (= o "-c") (= o "--clojure")) (keyword "clojure")
    (or (= o "-i") (= o "--inventory")) (keyword "inventory")
    (keyword? :else) (do
                       (println (str "Parameter not found: " o))
                       (help)
                       (exit! 1))))

(defn read-args []
  (let [n (count *ARGV*)]
    (cond
      (= n 0) (do (help) (exit! 1))
      (and (= n 1)
           (or (= (first *ARGV*) "-h") (= (first *ARGV*) "--help"))) (do
                                                                       (help)
                                                                       (exit! 0))
      (odd? n) (do
                 (println "Parameter error:")
                 (help)
                 (exit! 1))
      (keyword? :else) (let [parameters (reduce
                                         (fn [a b] (merge a b)) {} (map
                                                                    (fn [s] (hash-map (options (nth *ARGV* s)) (nth *ARGV* (+ 1 s)))) (range 0 n 2)))]
                         parameters))))
