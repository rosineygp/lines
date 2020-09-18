(load-file-without-hashbang "src/includes/lang-utils.clj")
(load-file-without-hashbang "src/includes/use.clj")
(load-file-without-hashbang "src/includes/colors.clj")

(load-file-without-hashbang "src/core.clj")

(load-file-without-hashbang "src/modules/pretty-print.clj")
(load-file-without-hashbang "src/modules/docker.clj")
(load-file-without-hashbang "src/modules/shell.clj")
(load-file-without-hashbang "src/modules/ssh.clj")

(use ["pwd"
      "pwd"
      "date"
      "git"])

(def current-path (nth (pwd) 0))
(def current-dir (last (str-split current-path "/")))
(def repos (str "/repos/" current-dir))

(def ttl 3600)
(def max-attempts 2)

(defn job [item]
  (let [start (time-ms)
        method (get item :method)
        retries (lines-retries (get item :retries))
        tasks (lines-job-retry retries (str "lines-job-" method) item)
        pipestatus (map (fn [l]
                          (map (fn [x] (get x :exit-code)) l)) tasks)
        status (lines-job-status (apply concat pipestatus))
        result (assoc item
                      :attempts (count tasks)
                      :start start
                      :finished (time-ms)
                      :pipestatus pipestatus
                      :status status
                      :tasks tasks)]
    (if (or status (get item :allow_failure)) result (throw result))))

(defn parallel [items]
  (pmap (fn [item] (job item)) items))
