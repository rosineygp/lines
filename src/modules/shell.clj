(str-use ["sudo"
          "ssh"
          "sshpass"])

(defn str-target-ssh [c]
  (let [user (or (get c :user) (env "USER"))
        port (or (get c :port) 22)
        common (ssh [(str user "@" (get c :host))
                     "-p"
                     port])]
    (cond
      (key-exist? c :key) (str-cmd [common
                                    "-i"
                                    (get c :key)])
      (key-exist? c :password) (sshpass ["-p"
                                         (str "'" (get c :password) "'")
                                         common])
      (keyword? :else) common)))

(defn str-shell-sudo [job]
  (let [user (or (get-in job [:target :user]) "root")
        sudo? (or (get-in job [:args :sudo]) false)]
    (if (or sudo? (not (= user "root")))
      (sudo ["-u " user "--"])
      "")))

(defn str-shell-variable [key value]
  (str key "=\"" value "\""))

(defn str-shell-job-variables [variables]
  (if (not (nil? variables))
    (str-cmd (map
              (fn [key]
                (str-shell-variable key (get variables key)))
              (keys variables))) ""))

(defn str-shell-entrypoint [entrypoint]
  (str-cmd (if (nil? entrypoint) ["bash" "-c"] entrypoint)))

(defn str-shell-command-line [job script-index]
  (let [branch (branch-or-tag-name)
        remote? (isremote? job)
        method (let [k (get-in job [:target :method])] (if (string? k) k "ssh"))]
    (str-cmd [(if remote? (str ((call (str "str-target-" method)) (get job :target)) " \"") "")
              (str-shell-sudo job)
              (str-shell-entrypoint (get-in job [:args :entrypoint]))
              "$'"
              "export"
              (str-shell-job-variables (get job :vars))
              ";"
              (str-escapes script-index)
              "'"
              (if remote? "\"" "")])))

(defn lines-module-shell [job]
  (lines-task-loop job str-shell-command-line))