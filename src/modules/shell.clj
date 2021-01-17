(str-use ["sudo"
          "ssh"])

(def! str-target-ssh 
  (fn* [c]
    (ssh [(str (or (get c :user) (env "USER")) "@" (get c :host)) 
          "-p" (or (get c :port) 22)])))

(def! str-shell-sudo
  (fn* [job]
       (let* [user (or (get-in job [:args :user]) "root")
              sudo? (or (get-in job [:args :sudo]) false)]
             (if (or sudo? (not (= user "root")))
               (sudo ["-u " user "--"]) ""))))

(def! str-shell-var
  (fn* [key value]
       (str key "=\"" value "\"")))

(def! str-shell-job-vars
  (fn* [vars]
       (if (not (nil? vars))
         (str-cmd (map
                   (fn* [key]
                        (str-shell-var key (get vars key)))
                   (keys vars))) "")))

(def! str-shell-entrypoint
  (fn* [entrypoint]
       (str-cmd (if (nil? entrypoint) ["bash" "-s"] entrypoint))))

(def! str-shell-command-line
  (fn* [job script-index]
       (let* [branch (branch-or-tag-name)
              method (let* [k (get-in job [:target :method])] (if (string? k) k "ssh"))]
             (str-cmd [(if (isremote? job) ((call (str "str-target-" method)) (get job :target)) "")
                       (str-shell-sudo job)
                       (str-shell-entrypoint (get-in job [:args :entrypoint]))
                       "<<-'LINES-BLOCK-EOF'\n"
                       "export" (str-shell-job-vars (get job :vars)) ";\n"
                       script-index
                       "\nLINES-BLOCK-EOF"]))))

(def! lines-module-shell
  (fn* [job]
       (lines-task-loop job str-shell-command-line)))