(str-use ["sudo"
          "ssh"])

(defn str-target-ssh [c]
    (ssh [(str (or (get c :user) (env "USER")) "@" (get c :host)) 
          "-p" (or (get c :port) 22)]))

(defn str-shell-sudo [job]
  (let [user (or (get-in job [:args :user]) "root")
        sudo? (or (get-in job [:args :sudo]) false)]
    (if (or sudo? (not (= user "root")))
      (sudo ["-u " user "--"]) "")))

(defn str-shell-var [key value]
  (str key "=\"" value "\""))

(defn str-shell-job-vars [vars]
  (if (not (nil? vars))
    (str-cmd (map
              (fn [key]
                (str-shell-var key (get vars key)))
              (keys vars))) ""))

(defn str-shell-entrypoint [entrypoint]
  (str-cmd (if (nil? entrypoint) ["bash" "-s"] entrypoint)))

(defn str-shell-command-line [job script-index]
  (let [branch (branch-or-tag-name)
        method (let [k (get-in job [:target :method])] (if (string? k) k "ssh"))]
    (str-cmd [(if (isremote? job) ((call (str "str-target-" method)) (get job :target)) "")
              (str-shell-sudo job)
              (str-shell-entrypoint (get-in job [:args :entrypoint]))
              "<<-'LINES-BLOCK-EOF'\n"
              "export" (str-shell-job-vars (get job :vars)) ";\n"
              script-index
              "\nLINES-BLOCK-EOF"])))

(defn lines-module-shell [job]
  (lines-task-loop job str-shell-command-line))