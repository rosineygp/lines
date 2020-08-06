(load-file-without-hashbang "src/includes/use.clj")
(load-file-without-hashbang "src/includes/colors.clj")

(use ["pwd"
      "docker"
      "pwd"
      "date"
      "git"])

(def current-path (nth (pwd) 0))
(def current-dir (last (str-split current-path "/")))
(def repos (str "/repos/" current-dir))

(def ttl 3600)

(defn str-date-time []
  (nth (date ["+'%Y-%m-%d %T'"]) 0))

(defn str-slug [string]
  (str-lower-case (reduce
                   (fn [a b] (str-replace a b "-"))
                   string [":" "." "/" "_"])))

(defn output-line-action [action]
  (println-stderr (green action)))

(defn output-line-banner [name]
  (println-stderr (str (bg-blue (bold (white (str  name " > " (str-date-time))))))))

(defn branch-or-tag-name []
  (cond
    (= (env "GITHUB_ACTIONS") "true") (env "GITHUB_REF")
    (= (env "GITLAB_CI") "true") (last (str-split (env "CI_COMMIT_REF_NAME") "/"))
    (string? (env "JENKINS_URL") nil) (env "GIT_BRANCH")
    (= (env "TRAVIS") "true") (env "TRAVIS_BRANCH")
    (= (env "CIRCLECI") "true") (if (string? (env "CIRCLE_TAG") "") (env "CIRCLE_TAG") (env "CIRCLE_BRANCH"))
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

(defn lines-error-handler [job instance exit-code]
  (if (> exit-code 0)
    (if (not (get job :allow_failure) true)
      (do
        (println (bg-red (white (bold "JOB FAILED"))))
        (lines-docker-rm instance)
        (exit! exit-code)))))

(defn lines-docker-run [job]
  (let [result (docker ["run"
                        "--detach"
                        "--rm"
                        "--entrypoint" "''"
                        (if (= (get job :privileged) true)
                          (apply join " " ["--privileged"
                                           "--volume"
                                           "/var/run/docker.sock:/var/run/docker.sock"]) "")
                        (if (get job :variables)
                          (apply join " " (map
                                           (fn [key]
                                             (str "--env '" key "=" (get (get job :variables) key) "'"))
                                           (keys (get job :variables)))) "")
                        "--workdir" repos
                        (get job :image)
                        "sleep" ttl])]
    (do
      (output-line-action (str "docker run: " (get job :image)))
      (print-command result)
      (if (> (nth result 2) 0)
        (exit! (nth result 2)))
      (nth result 0))))

(defn lines-docker-cp-push [instance from to]
  (let [result (docker ["cp"
                        from
                        (str instance ":" to)])]
    (do
      (output-line-action (str "docker cp: from " from " to " instance ":" to))
      (print-command result)
      (if (> (nth result 2) 0)
        (exit! (nth result 2))))))

(defn lines-docker-exec [job instance command]
  (let [result (docker ["exec"
                        "--tty"
                        "--interactive"
                        instance
                        "sh" "-c '" command "'"])]
    (do
      (output-line-action (str "docker exec: " command))
      (print-command result)
      (lines-error-handler job instance (nth result 2)))))

(defn lines-docker-rm [instance]
  (let [result (docker ["rm"
                        "--force"
                        instance])]
    (do
      (output-line-action (str "docker rm: " instance))
      (print-command result))))

(defn lines-docker-job [job]
  (let [instance (lines-docker-run job)]
    (do
      (lines-docker-cp-push instance current-path "/repos")
      (map (fn* [code-line]
                (lines-docker-exec job instance code-line))
           (get job :script))
      (lines-docker-rm instance))))

(defn job [item]
  (do
    (output-line-banner (str "begin: " (get item :name)))
    (if (= (get item :method) "docker")
      (do
        (lines-docker-job item)))
    (output-line-banner (str "done: " (get item :name)))))

(defn parallel [items]
  (pmap (fn* [item] (job item)) items))
