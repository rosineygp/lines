(defn lines-shell-sudo [job]
    (if (= (get job :sudo) true)
        "sudo"
        ""))

(defn lines-shell-exec [job command]
  (let [sudo (lines-shell-sudo job)
        result (sh! (str sudo " " command))]
    (do
      (print-command result)
      (if (> (nth result 2) 0)
        (throw (str "exit-code " (nth result 2)))
        result))))

(defn lines-shell-job [job]
  (do
    (try*
     (map (fn [command]
            (do
              (output-line-action (str "shell: " (magenta (lines-shell-sudo job)) " " (white command)))
              (lines-shell-exec job command)))
          (get job :script))
     (catch* ex
             (let [exit-code (last (str-split ex " "))]
               (do
                 (println (bg-red (white (bold "JOB FAILED"))))))))))