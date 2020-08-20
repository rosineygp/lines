(load-file-without-hashbang "src/includes/use.clj")
(load-file-without-hashbang "src/includes/colors.clj")
(load-file-without-hashbang "src/core.clj")
(load-file-without-hashbang "src/docker.clj")
(load-file-without-hashbang "src/shell.clj")
(load-file-without-hashbang "src/ssh.clj")

(use ["pwd"
      "pwd"
      "date"
      "git"])

(def current-path (nth (pwd) 0))
(def current-dir (last (str-split current-path "/")))
(def repos (str "/repos/" current-dir))

(def ttl 3600)
(def max-attempts 3)

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
  (let [method (get item :method)
        retries (if (get item :retries)
                  (atom (if (> (get item :retries) max-attempts) max-attempts (get item :retries)))
                  (atom 1))]
    (do
      (output-line-banner (str "begin: " (get item :name)))
      (try*
       (lines-job-method item retries)
       (catch* ex
               (let [error (lines-throw-split ex)]
                 (do
                   (lines-output-error (get error :message))
                   (exit! (get error :exit-code))))))
      (output-line-banner (str "done: " (get item :name))))))

(defn parallel [items]
  (pmap (fn [item] (job item)) items))
