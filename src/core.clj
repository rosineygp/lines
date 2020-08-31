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

(defn lines-task-obj [start raw-cmd cmd result finished]
  {:start start
   :raw-cmd raw-cmd
   :cmd cmd
   :stdout (nth result 0)
   :stderr (nth result 1)
   :exit-code (nth result 2)
   :finished finished})