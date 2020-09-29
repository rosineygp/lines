(defn str-connection-ssh [c]
  (let [user (if (get c :user) (get c :user) (env "USER"))
        port (if (get c :port) (get c :port) 22)
        common (apply str-join " " ["ssh"
                                    (str user "@" (get c :host))
                                    "-p"
                                    port])]
    (cond
      (key-exist? c :key) (apply str-join " " [common
                                               "-i"
                                               (get c :key)])
      (key-exist? c :password) (apply str-join " " ["sshpass"
                                                    "-p"
                                                    (str "'" (get c :password) "'")
                                                    common])
      (keyword? :else) common)))

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
        remote? (if (get job :connection) (if (= (get (get job :connection) :method) "local") false true) false)
        method (if remote? (if (get (get job :connection) :method) (get (get job :connection) :method) "ssh"))]
    (apply str-join " " [(if remote? (str ((call (str "str-connection-" method)) (get job :connection)) " \"") "")
                         (str-shell-sudo job)
                         (str-shell-entrypoint (get job :entrypoint))
                         "$'"
                         "export"
                         (str-shell-variable "BRANCH_NAME" branch)
                         (str-shell-variable "BRANCH_NAME_SLUG" (str-slug branch))
                         (str-shell-job-variables (get job :variables))
                         ";"
                         (str-escapes script-index)
                         "'"
                         (if remote? "\"" "")])))

(defn lines-job-shell [job]
  (lines-task-loop job str-shell-command-line))