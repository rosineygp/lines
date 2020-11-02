(defn help []
  (println "Usage: lines [OPTION]... [FILE]...
A pure bash clojureish CI pipeline.
version: 0.0.0

Options:
-i, --inventory     inventory file.
-p, --pipeline      pipeline file.
-j, --job           run only named job name.
-c, --clojure       for clj file (pure clojure pipeline)
-v, --version       show current version.

Otherwise:
run .lines.edn or .lines.clj"))

(defn version []
  (println "lines, version: 0.0.0"))

(defn help-and-exit [exit-code]
  (do
    (help)
    (exit! (or exit-code 0))))

(defn options [o]
  (cond
    (or (= o "-p") (= o "--pipeline")) (keyword "pipeline")
    (or (= o "-c") (= o "--clojure")) (keyword "clojure")
    (or (= o "-i") (= o "--inventory")) (keyword "inventory")
    (or (= o "-j") (= o "--job")) (keyword "job-name")
    (keyword? :else) (do
                       (println (str "Parameter not found: " o))
                       (help)
                       (exit! 1))))

(defn read-args []
  (let [n (count *ARGV*)]
    (cond
      (= n 0) (cond
                (file-exists? ".lines.edn") (hash-map :pipeline ".lines.edn")
                (file-exists? ".lines.clj") (hash-map :clojure ".lines.clj")
                (keyword? :else) (help-and-exit 1))
      (= n 1) (cond
                (or (= (first *ARGV*) "-h")
                    (= (first *ARGV*) "--help")) (help-and-exit 0)
                (or (= (first *ARGV*) "-v")
                    (= (first *ARGV*) "--version")) (do
                                                      (version)
                                                      (exit! 0))
                (keyword? :else) (help-and-exit 1))           
      (odd? n) (do
                 (println "Parameter error:")
                 (help)
                 (exit! 1))
      (keyword? :else) (let [parameters (reduce
                                         (fn [a b] (merge a b)) {} (map
                                                                    (fn [s] (hash-map (options (nth *ARGV* s)) (nth *ARGV* (+ 1 s)))) (range 0 n 2)))]
                         parameters))))
