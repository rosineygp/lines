(defn lines-shell-sudo [job]
  (if (= (get job :sudo) true)
    "sudo"
    ""))

(defn lines-shell-exec [job command]
  (let [sudo (lines-shell-sudo job)
        result (sh! (apply str-join " " [(apply str-join " " (if (get job :entrypoint)
                                                               (get job :entrypoint)
                                                               ["bash" "-c"]))
                                        "'"
                                        (str "BRANCH_NAME=" (branch-or-tag-name))
                                        (if (get job :variables)
                                          (apply str-join " " (map
                                                     (fn [key]
                                                       (str key "=" (get (get job :variables) key)))
                                                     (keys (get job :variables)))) "")
                                         ";"
                                         sudo
                                         command 
                                         "'"]))]
    (do
      (print-command result)
      (lines-throw-command result))))

(defn lines-shell-job [job]
  
    (do
      (println @retries)
      (try*
      (map (fn [command]
              (do
                (output-line-action (apply str-join " " ["shell:"
                                                        (magenta (lines-shell-sudo job))
                                                        (white command)]))
                (lines-shell-exec job command)))
            (get job :script))
      (catch* ex
              (let [tt "e"]
                (do
                  (println (lines-throw-split ex))
                  (swap! retries dec)
                  (lines-shell-job job)
                  
                  (println (bg-red (white (bold "JOB FAILED"))))))))))