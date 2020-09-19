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

(defn lines-job-shell [job]
  (do
    (lines-task-loop job str-shell-command-line)))