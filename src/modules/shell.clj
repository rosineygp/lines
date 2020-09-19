(defn str-shell-sudo [job]
  (let [user (if (get job :user) (get job :user) "root")
        sudo? (if (= (get job :sudo) true) true false)]
    (if (or sudo? (not (= user "root")))
      (str "sudo -u " user " --")
      "")))

(defn str-shell-variable [key value]
  (str key "=\"" value "\""))

(defn str-shell-job-variables [variables]
  (if (not (nil? variables))
    (apply str-join " " (map
                         (fn [key]
                           (str-shell-variable key (get variables key)))
                         (keys variables))) ""))

(defn str-shell-entrypoint [entrypoint]
  (apply str-join " " (if (nil? entrypoint) ["bash" "-c"] entrypoint)))

(defn str-shell-command-line [job script-index]
  (let [branch (branch-or-tag-name)]
    (apply str-join " " [(str-shell-sudo job)
                         (str-shell-entrypoint (get job :entrypoint))
                         "$'"
                         "export"
                         (str-shell-variable "BRANCH_NAME" branch)
                         (str-shell-variable "BRANCH_NAME_SLUG" (str-slug branch))
                         (str-shell-job-variables (get job :variables))
                         ";"
                         (str-escapes script-index)
                         "'"])))

(defn lines-tasks-allow-failure [allow_failure]
  (if allow_failure true false))

(defn lines-tasks-break [result]
  (if (> (get result :exit-code) 0) (throw result) result))

(defn lines-task-execute [cmd]
  (let [start (time-ms)
        result (sh! cmd)
        finished (time-ms)]
    {:start start
     :finished finished
     :stdout (nth result 0)
     :stderr (nth result 1)
     :exit-code (nth result 2)
     :debug cmd}))

(defn lines-task-loop [j f & more]
  (let [l (get j :script)
        t (- (count l) 1)
        break (atom 0)]
    (filter (fn [x] (map? x)) (map
                               (fn [i] (if (= @break 0)
                                         (let [str-cmd (apply f j (nth l i) more)
                                               r (lines-task-execute str-cmd)]
                                           (if (> (get r :exit-code) 0) (swap! break inc))
                                           r) nil))
                               (range t)))))

(defn lines-job-shell [job]
  (do
    (lines-task-loop job str-shell-command-line)))