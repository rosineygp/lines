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
  (let [branch (branch-or-tag-name)
        script-line (lines-job-script-by-index script-index)]
    (apply str-join " " [(str-shell-sudo job)
                         (str-shell-entrypoint (get job :entrypoint))
                         "$'"
                         "export"
                         (str-shell-variable "BRANCH_NAME" branch)
                         (str-shell-variable "BRANCH_NAME_SLUG" (str-slug branch))
                         (str-shell-job-variables (get job :variables))
                         ";"
                         (str-escapes script-line)
                         "'"])))

(defn lines-shell-exec [job script-index]
  (let [start (time-ms)
        sudo (str-shell-sudo job)
        branch (branch-or-tag-name)
        cmd  (str-shell-command-line job script-index)
        result (sh! cmd)
        finished (time-ms)]
    (do
      (lines-throw-command result)
      (lines-task-obj start
                      finished
                      (nth result 0)
                      (nth result 1)
                      (nth result 2)
                      script-line
                      cmd))))

(defn lines-shell-job [job]
  (do
    (map (fn [script-line]
           (do
             (lines-shell-exec job script-line)))
         (get job :script))))