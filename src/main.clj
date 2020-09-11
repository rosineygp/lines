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
(def max-attempts 3)

(defn lines-job-status [pipestatus]
  (let [exit-sum (reduce (fn [a b] (+ a b)) 0 pipestatus)]
    (if (= exit-sum 0) true false)))

(defn lines-job-method [job retries]
  (let [method (get job :method)]
    (do
      (if (> @retries 0)
        (try*
         (cond
           (= method "docker") (do (lines-docker-job job))
           (= method "shell") (do (lines-shell-job job))
           (= method "ssh") (do (lines-ssh-job job)))
         (catch* ex
                 (do
                   (swap! retries dec)
                   (lines-output-error (str "Command failed: " @retries "/" max-attempts))
                   (lines-job-method job retries))))
        (throw "exit-code=1,message=Error in all attempts.")))))

(defn job [item]
  (let [start (time-ms)
        method (get item :method)
        retries (if (get item :retries)
                  (atom (if (> (get item :retries) max-attempts) max-attempts (get item :retries)))
                  (atom 1))
        tasks (try*
               (lines-job-method item retries)
               (catch* ex
                       (let [error (lines-throw-split ex)]
                         (throw (str "exit-code=" (get error :exit-code) ",message=Job " (get item :name) " failed.")))))
        pipestatus (map (fn [x] (get x :exit-code)) tasks)
        result (assoc item
                      :start start
                      :finished (time-ms)
                      :pipestatus pipestatus
                      :success (lines-job-status pipestatus)
                      :tasks tasks)]
    (do
      (println result)
      result)))

(defn parallel [items]
  (pmap (fn [item] (job item)) items))
