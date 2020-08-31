(defn lines-shell-sudo [job]
  (let [user (if (get job :user) (get job :user) "root")
        sudo? (if (= (get job :sudo) true) true false)]
    (if (or sudo? (not (= user "root")))
      (str "sudo -u " user " --")
      "")))

(defn lines-shell-exec [job raw-cmd]
  (let [start (time-ms)
        sudo (lines-shell-sudo job)
        branch (branch-or-tag-name)
        cmd  (apply str-join " " [sudo
                                  (apply str-join " " (if (get job :entrypoint)
                                                        (get job :entrypoint)
                                                        ["bash" "-c"]))
                                  "$'"
                                  "export"
                                  (str-shell-variable "BRANCH_NAME" branch)
                                  (str-shell-variable "BRANCH_NAME_SLUG" (str-slug branch))
                                  (if (get job :variables)
                                    (apply str-join " " (map
                                                         (fn [key]
                                                           (str-shell-variable key (get (get job :variables) key)))
                                                         (keys (get job :variables)))) "")
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