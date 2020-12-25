(defn help []
  (println (str "Usage: lines [OPTION]...
A pure bash clojureish CI pipeline.
\x00
Options:
-i, --inventory           inventory file.
-p, --pipeline            pipeline file.
-j, --filter-job          filter jobs by any keyword. 
\x00                          ex: 'name=job 1', 'groups=lint'.
-l, --filter-inventory    filter hosts by any keyword. 
\x00                          ex 'label=linux01', 'method=ssh'.
-o, --output              output method: [default, minimal, edn].
\x00                          otherwise: default
-c, --clojure             for clj file (pure clojure pipeline)
-r, --repl                Lines console repl.
-v, --version             show current version.
\x00
Otherwise:
lookup for .lines.edn or .lines.clj\n\n" (version))))

(defn version [] "Lines, version: v1.0.0")

(defn help-and-exit [exit-code]
  (do
    (help)
    (exit! (or exit-code 0))))

(defn options [o]
  (cond
    (or (= o "-p") (= o "--pipeline")) (keyword "pipeline")
    (or (= o "-c") (= o "--clojure")) (keyword "clojure")
    (or (= o "-i") (= o "--inventory")) (keyword "inventory")
    (or (= o "-j") (= o "--filter-job")) (keyword "filter-job")
    (or (= o "-l") (= o "--filter-inventory")) (keyword "filter-inventory")
    (or (= o "-o") (= o "--output")) (keyword "output")
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
                (or (= (first *ARGV*) "-r")
                    (= (first *ARGV*) "--repl")) (hash-map :repl true)
                (or (= (first *ARGV*) "-v")
                    (= (first *ARGV*) "--version")) (do
                                                      (println (version))
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

(def args (read-args))