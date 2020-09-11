(defn lines-ssh-sudo [job]
  (if (= (get job :sudo) true)
    "sudo"
    ""))

(defn lines-ssh-exec [job command]
  (let [conn (get job :connection)
        sudo (lines-ssh-sudo job)
        result (sh! (str "sshpass -p '" (get conn :password) "' ssh " (get conn :user) "@" (get conn :host) " '" command "'"))]
    (do
      (print-command result)
      (if (> (nth result 2) 0)
        (throw (str "exit-code " (nth result 2)))
        result))))

(defn lines-ssh-job [job]
  (do
    (try*
     (map (fn [command]
            (do
              (output-line-action (str "shell: " (magenta (lines-ssh-sudo job)) " " (white command)))
              (lines-ssh-exec job command)))
          (get job :script))
     (catch* ex
             (let [exit-code (last (str-split ex " "))]
               (do
                 (println (bg-red (white (bold "JOB FAILED"))))))))))