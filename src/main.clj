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
                   string [":" "." "/" "_" " "])))

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

(defn lines-docker-error-instance [result]
  (if (> (nth result 2) 0)
    (exit! (nth result 2))
    (nth result 0)))

(defn lines-traps [list]
  (trap! (str "{ " (apply str-join "; " (map (fn [item]
                                               (if (= (get item :type) "container")
                                                 (str "docker rm -f " (get item :id))
                                                 (str "docker network rm " (get item :id)))) list)) "; }") "EXIT"))

(defn lines-docker-network [job]
  (let [network-name (str-slug (str (get job :name) (time-ms)))
        result (docker ["network"
                        "create"
                        network-name])]
    (do
      (output-line-action (str "docker network: " (white network-name)))
      (print-command result)
      (if (> (nth result 2) 0)
        (exit! (nth result 2)))
      (nth result 0))))

(defn lines-docker-network-rm [instance]
  (let [result (docker ["network"
                        "rm"
                        instance])]
    (do
      (output-line-action (str "docker network rm: " (white (str-subs instance 0 12))))
      (print-command result))))

(defn lines-docker-run [job network]
  (let [result (docker ["run"
                        "--detach"
                        "--rm"
                        "--network" network
                        "--entrypoint" "''"
                        "--env" (str "'BRANCH_NAME=" (branch-or-tag-name) "'")
                        (if (= (get job :privileged) true)
                          (apply str-join " " ["--privileged"
                                               "--volume"
                                               "/var/run/docker.sock:/var/run/docker.sock"]) "")
                        (if (get job :variables)
                          (apply str-join " " (map
                                               (fn [key]
                                                 (str "--env '" key "=" (get (get job :variables) key) "'"))
                                               (keys (get job :variables)))) "")
                        "--workdir" repos
                        (get job :image)
                        "sleep" ttl])]
    (do
      (output-line-action (str "docker run: " (white (get job :image))))
      (print-command result)
      (lines-docker-error-instance result))))

(defn lines-docker-run-service [service network]
  (let [result (docker ["run"
                        "--detach"
                        "--rm"
                        "--network" network
                        "--network-alias" (if (get service :alias)
                                            (get service :alias)
                                            (str-slug (get service :image)))
                        "--env" (str "'BRANCH_NAME=" (branch-or-tag-name) "'")
                        (if (get service :variables)
                          (apply str-join " " (map
                                           (fn [key]
                                             (str "--env '" key "=" (get (get service :variables) key) "'"))
                                           (keys (get service :variables)))) "")
                        (if (get service :entrypoint)
                          (str "--entrypoint " (get service :entrypoint)) "")
                        (get service :image)])]
    (do
      (trap! (str "docker rm -f " (nth result 0)) "EXIT")
      (output-line-action (str "docker service: " (white (get service :image))))
      (print-command result)
      (lines-docker-error-instance result))))

(defn lines-docker-cp-push [instance from to]
  (let [result (docker ["cp"
                        from
                        (str instance ":" to)])]
    (do
      (output-line-action (str "docker cp: " (white (str "from " from " to " (str-subs instance 0 12) ":" to))))
      (print-command result)
      (lines-docker-error-instance result))))

(defn lines-docker-exec! [job instance command]
  (let [result (docker ["exec"
                        "--tty"
                        "--interactive"
                        instance
                        (apply str-join " " (if (get job :entrypoint)
                                              (get job :entrypoint)
                                              ["sh" "-c"]))
                        "'" command "'"])]
    (do
      (print-command result)
      (if (> (nth result 2) 0)
        (throw (str "exit-code " (nth result 2)))
        result))))

(defn lines-docker-rm [instance]
  (let [result (docker ["rm"
                        "--force"
                        instance])]
    (do
      (output-line-action (str "docker rm: " (white (str-subs instance 0 12))))
      (print-command result))))

(defn lines-docker-job-rm [instance services network]
  (do
    (lines-docker-rm instance)
    (map lines-docker-rm services)
    (lines-docker-network-rm network)))

(defn lines-docker-job [job]
  (let [network (lines-docker-network job)
        services (if (get job :services)
                   (do
                     (map (fn [service]
                            (lines-docker-run-service service network)) (get job :services))))
        instance (lines-docker-run job network)]
    (do
      (lines-traps (concat [{:id instance :type "container"}]
                           (map (fn [item] {:id item :type "container"}) services)
                           [{:id network :type "network"}]))
      (lines-docker-cp-push instance current-path "/repos")
      (try*
       (map (fn [code-line]
              (do
                (output-line-action (str "docker exec: " (white code-line)))
                (lines-docker-exec! job instance code-line)))
            (get job :script))
       (catch* ex
               (let [exit-code (last (str-split ex " "))]
                 (do
                   (println (bg-red (white (bold "JOB FAILED"))))))))
      (lines-docker-job-rm instance
                           services
                           network))))

(defn job [item]
  (do
    (output-line-banner (str "begin: " (get item :name)))
    (if (= (get item :method) "docker")
      (do
        (lines-docker-job item)))
    (output-line-banner (str "done: " (get item :name)))))

(defn parallel [items]
  (pmap (fn [item] (job item)) items))
