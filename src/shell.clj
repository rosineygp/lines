(defn lines-shell-exec [job raw-cmd]
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
                                  (str-escapes raw-cmd)
                                  "'"])
        result (sh! cmd)
        finished (time-ms)]
    (do
      (lines-throw-command result)
      (lines-task-obj start raw-cmd cmd result finished))))

(defn lines-shell-job [job]
  (do
    (map (fn [cmd]
           (do
             (lines-shell-exec job cmd)))
         (get job :script))))