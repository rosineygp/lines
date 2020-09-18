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

(defn lines-job-status [l]
  (let [exit-sum (reduce (fn [a b] (+ a b)) 0 l)]
    (if (= exit-sum 0) true false)))

(defn lines-retries [r]
  (if r (if (> r max-attempts) max-attempts r) 0))

(defn lines-job-retry-inner [retries k! f job]
  (let [r ((call f) job)
        retry? (reduce (fn [a b] (+ a b)) 0 (map (fn [x] (get x :exit-code)) r))]
    (do
      (swap! k! concat [r])
      (if (> retry? 0)
        (if (> retries 0)
          (lines-job-retry-inner (dec retries) k! f job))))))
    
(defn lines-job-retry [retries f job]
  (let [k (atom [])]
    (do
      (lines-job-retry-inner retries k f job)
      @k)))

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
