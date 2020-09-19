(defn str-date-time []
  (nth (date ["+'%Y-%m-%d %T'"]) 0))

(defn str-slug [string]
  (str-lower-case (reduce
                   (fn [a b] (str-replace a b "-"))
                   string [":" "." "/" "_" " "])))

(defn str-escapes [string]
  (reduce
   (fn [a b] (str-replace a b (str "\\" b)))
   string ["'"]))

(defn output-line-action [action]
  (println-stderr (green action)))

(defn output-line-banner [name]
  (println-stderr (str (bg-blue (bold (white (str  name " > " (str-date-time))))))))

(defn lines-output-error [message]
  (println-stderr (str (bg-red (bold (white message))))))

(defn branch-or-tag-name []
  (cond
    (= (env "GITHUB_ACTIONS") "true") (last (str-split (env "GITHUB_REF") "/"))
    (= (env "GITLAB_CI") "true") (env "CI_COMMIT_REF_NAME")
    (= (string? (env "JENKINS_URL")) true) (env "GIT_BRANCH")
    (= (env "TRAVIS") "true") (env "TRAVIS_BRANCH")
    (= (env "CIRCLECI") "true") (if (string? (env "CIRCLE_TAG")) (env "CIRCLE_TAG") (env "CIRCLE_BRANCH"))
    (= (nth (git ["rev-parse"
                  "--is-inside-work-tree"]) 0) "true") (nth (git ["rev-parse"
                                                                  "--abbrev-ref"
                                                                  "HEAD"]) 0)))

(defn print-command [result]
  (let [std (nth result 0)
        err (nth result 1)
        exit-code (nth result 2)]
    (if (= (empty? std) false) (println std))
    (if (= (empty? err) false) (println-stderr (red err)))
    (if (number? exit-code) (println-stderr (magenta exit-code)))))

(defn lines-throw-command [result]
  (if (> (nth result 2) 0)
    (throw (apply str-join "," [(apply str-join "=" ["exit-code" (nth result 2)])
                                (apply str-join "=" ["message" "Exit code greater than 0."])])) result))

(defn lines-throw-split [string]
  (reduce (fn [a b]
            (assoc a (first (keys b)) (first (vals b)))) {}
          (map (fn [i]
                 (let [key-val (str-split i "=")]
                   (do
                     (hash-map (keyword (nth key-val 0)) (nth key-val 1))))) (str-split string ","))))

(defn lines-job-script-by-index [job index]
  (nth (get job :script) index))

(defn lines-task-obj [start finished stdout stderr exit-code script-line debug]
  {:start start
   :finished finished
   :stdout stdout
   :stderr stderr
   :exit-code exit-code
   :script-line script-line
   :debug debug})

; job handler
(defn lines-job-status [l]
  (let [exit-sum (reduce (fn [a b] (+ a b)) 0 l)]
    (if (= exit-sum 0) true false)))

(defn lines-retries [r]
  (if r (if (> r max-attempts) max-attempts r) 0))

(defn lines-job-retry-inner [retries k! f job]
  (let [r ((call f) job)
        retry? (reduce (fn [a b] (+ a b)) 0 (map (fn [x] (get x :exit-code)) r))]
    (do
      (swap! k! concat [r])
      (if (> retry? 0)
        (if (> retries 0)
          (lines-job-retry-inner (dec retries) k! f job))))))

(defn lines-job-retry [retries f job]
  (let [k (atom [])]
    (do
      (lines-job-retry-inner retries k f job)
      @k)))

; task handler
(defn lines-tasks-allow-failure [allow_failure]
  (if allow_failure true false))

(defn lines-tasks-break [result]
  (if (> (get result :exit-code) 0) (throw result) result))

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