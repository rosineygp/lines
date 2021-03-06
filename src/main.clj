(load-file-without-hashbang "src/includes/lang-utils.clj")
(load-file-without-hashbang "src/args.clj")
(load-file-without-hashbang "src/includes/use.clj")
(load-file-without-hashbang "src/includes/colors.clj")
(load-file-without-hashbang "src/includes/pretty-print.clj")
(load-file-without-hashbang "src/core.clj")
(load-file-without-hashbang "src/modules/docker.clj")
(load-file-without-hashbang "src/modules/shell.clj")
(load-file-without-hashbang "src/modules/template.clj")
(load-file-without-hashbang "src/modules/scp.clj")

(use ["pwd"])

(def! current-path (nth (pwd) 0))
(def! current-dir (last (str-split current-path "/")))
(def! repos (str "/repos/" current-dir))
(def! temp-dir "/tmp/")

(def! ttl (or (env "LINES_JOB_TTL") 3600))
(def! max-attempts (or (env "LINES_JOB_MAX_ATTEMPTS") 2))
(def! modules-dir (or (env "LINES_MODULES_DIR") ".lines/modules/"))
(def! ext-dir (or (env "LINES_EXT_DIR") ".lines/ext/"))

(if (dir-exists? ext-dir)
  (let* [l  (filter (fn* [i] (= (get i :type) "clj")) (list-dir ext-dir))]
        (if (map? (first l)) (map (fn* [i] (load-once (get i :object))) l))))

(cond
  (get args :repl) (repl)
  (get args :clojure) (load-file (get args :clojure))
  (get args :pipeline) (let* [o (or (get args :output) "default")
                              r (pipeline {:jobs (read-string (slurp (get args :pipeline)))
                                           :inventory (if (get args :inventory) (read-string (slurp (get args :inventory))))
                                           :filter-job (get args :filter-job)
                                           :filter-inventory (get args :filter-inventory)})]
                             (cond
                               (= o "minimal") (lines-pp-minimal r)
                               (= o "edn") (prn r)
                               (keyword? :else) (lines-pp r))))

; flk end
nil