(load-file-without-hashbang "src/includes/lang-utils.clj")
(load-file-without-hashbang "src/args.clj")

(def args (read-args))

(load-file-without-hashbang "src/includes/use.clj")
; (load-file-without-hashbang "src/includes/colors.clj")

(load-file-without-hashbang "src/core.clj")
; (load-file-without-hashbang "src/modules/pretty-print.clj")
(load-file-without-hashbang "src/modules/docker.clj")
(load-file-without-hashbang "src/modules/shell.clj")
(load-file-without-hashbang "src/modules/scp.clj")

(use ["pwd"])

(def current-path (nth (pwd) 0))
(def current-dir (last (str-split current-path "/")))
(def repos (str "/repos/" current-dir))

(def ttl 3600)
(def max-attempts 2)

(pipeline (get args :pipeline))
