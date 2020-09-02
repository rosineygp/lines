(defn lines-shell-exec [job script-line]
  (let [start (time-ms)
        sudo (str-shell-sudo job)
        branch (branch-or-tag-name)
        cmd  (apply str-join " " [sudo
                                  (str-shell-entrypoint (get job :entrypoint))
                                  "$'"
                                  "export"
                                  (str-shell-variable "BRANCH_NAME" branch)
                                  (str-shell-variable "BRANCH_NAME_SLUG" (str-slug branch))
                                  (str-shell-job-variables (get job :variables))
                                  ";"
                                  (str-escapes script-line)
                                  "'"])
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