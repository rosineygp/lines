(defn lines-shell-sudo [job]
  (if (= (get job :sudo) true)
    "sudo"
    ""))

(defn lines-shell-exec [job command]
  (let [start (time-ms)
        sudo (lines-shell-sudo job)
        str-cmd (apply str-join " " [(apply str-join " " (if (get job :entrypoint)
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
                                     "'"])
        result (sh! str-cmd)
        finished (time-ms)]
    (do
      (lines-throw-command result)
      (lines-hash-command start str-cmd result finished))))

(defn lines-shell-job [job]
  (do
    (map (fn [command]
           (do
             (lines-shell-exec job command)))
         (get job :script))))