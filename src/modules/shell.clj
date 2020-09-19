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

; (defn lines-shell-exec [job script-index]
;   (let [start (time-ms)
;         sudo (str-shell-sudo job)
;         branch (branch-or-tag-name)
;         cmd  (str-shell-command-line job script-index)
;         result (sh! cmd)
;         finished (time-ms)]
;     (do
;       (lines-throw-command result)
;       (lines-task-obj start
;                       finished
;                       (nth result 0)
;                       (nth result 1)
;                       (nth result 2)
;                       script-line
;                       cmd))))

; (defn lines-shell-exec [job script-index]
;   (let [start (time-ms)
;         sudo (str-shell-sudo job)
;         branch (branch-or-tag-name)
;         cmd  (str-shell-command-line job script-index)
;         finished (time-ms)]
;       (println cmd)
;        (cmd)))

; (defn lines-job-method [job retries]
;   (let [method (get job :method)]
;     (do
;       (if (> @retries 0)
;         (try*
;           (do ((call (str "lines-" method "-job")) job ))
;          (catch* ex
;                  (do
;                    (swap! retries dec)
;                    (lines-output-error (str "Command failed: " @retries "/" max-attempts))
;                    (lines-job-method job retries))))
;         (throw "exit-code=1,message=Error in all attempts.")))))

; (defn job [item]
;   (let [start (time-ms)
;         method (get item :method)
;         retries (if (get item :retries)
;                   (atom (if (> (get item :retries) max-attempts) max-attempts (get item :retries)))
;                   (atom 1))
;         tasks (try*
;                (lines-job-method item retries)
;                (catch* ex
;                        (let [error (lines-throw-split ex)]
;                          (throw (str "exit-code=" (get error :exit-code) ",message=Job " (get item :name) " failed.")))))
;         pipestatus (map (fn [x] (get x :exit-code)) tasks)
;         result (assoc item
;                       :start start
;                       :finished (time-ms)
;                       :pipestatus pipestatus
;                       :success (lines-job-status pipestatus)
;                       :tasks tasks)]
;     (do
;       (println result)
;       result)))



; :allow_failure true
(defn lines-tasks-allow-failure [allow_failure]
  (if allow_failure true false))


; (defn lines-tasks-retries [j]
;   )

(defn lines-tasks-break [result]
  (if (> (get result :exit-code) 0) (throw result) result))

; https://stackoverflow.com/questions/12068640/retrying-something-3-times-before-throwing-an-exception-in-clojure

; (defn retry-inner [retries cmd k!]
;   (let [r (lines-task-execute cmd)]
;     (do
;       (swap! k! concat [r])
;       (if (> (get r :exit-code) 0)
;         (if (> retries 0)
;           (retry-inner (dec retries) cmd k!))))))
  
; (defn retry [retries cmd]
;   (let [k (atom [])]
;     (do
;       (retry-inner retries cmd k)
;       @k)))

(defn lines-task-execute [cmd]
  (let [start (time-ms)
        result (sh! cmd)
        finished (time-ms)]
    {:start start
     :finished finished
     :stdout (nth result 0)
     :stderr (nth result 1)
     :exit-code (nth result 2)
     :debug cmd}))

(defn lines-task-loop [j f & more]
  (let [l (get j :script)
        t (- (count l) 1)
        break (atom 0)]
    (filter (fn [x] (map? x)) (map
                               (fn [i] (if (= @break 0)
                                         (let [str-cmd (apply f j (nth l i) more)
                                               r (lines-task-execute str-cmd)]
                                           (if (> (get r :exit-code) 0) (swap! break inc))
                                           r) nil))
                               (range t)))))

(defn lines-job-shell [job]
  (do
    (lines-task-loop job str-shell-command-line)))