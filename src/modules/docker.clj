(use ["docker"])

(defn lines-docker-traps [list]
    (trap! (str "{ " (apply str-join "; " (map (fn [item]
                                                 (if (= (get item :type) "container")
                                                   (str "docker rm -f " (get item :id))
                                                   (str "docker network rm " (get item :id)))) list)) "; }") "EXIT"))

(defn lines-docker-error-instance [result]
    (if (> (nth result 2) 0)
      (exit! (nth result 2))
      (nth result 0)))

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

(defn str-lines-docker-network [job network-name]
  (apply str-join " "  ["docker"
                        "network"
                        "create"
                        network-name]))
      
(defn lines-docker-network-rm [instance]
  (let [result (docker ["network"
                        "rm"
                        instance])]
    (do
      (output-line-action (str "docker network rm: " (white (str-subs instance 0 12))))
      (print-command result))))

(defn str-lines-docker-network-rm [network-name]
  (apply str-join " " ["docker"
                       "network"
                       "rm"
                       network-name]))
      
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

(defn str-lines-docker-run [job instance network]
  (apply str-join " " ["docker"
                       "run"
                       "--detach"
                       "--rm"
                       "--network" network
                       "--name" instance
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
                       "sleep" ttl]))
      
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

(defn str-lines-docker-run-service [service instance network]
  (apply str-join " " ["docker"
                       "run"
                       "--detach"
                       "--rm"
                       "--network" network
                       "--name" instance
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
                       (get service :image)]))
      
(defn lines-docker-cp-push [instance from to]
  (let [result (docker ["cp"
                        from
                        (str instance ":" to)])]
    (do
      (output-line-action (str "docker cp: " (white (str "from " from " to " (str-subs instance 0 12) ":" to))))
      (print-command result)
      (lines-docker-error-instance result))))

(defn str-lines-docker-cp-push [instance from to]
  (apply str-join " " ["docker"
                       "cp"
                       from
                       (str instance ":" to)]))
  
(defn lines-docker-cp-pull [instance from]
  (let [result (docker ["cp"
                        (str instance ":" repos "/" from)
                        "."])]
    (do
      (output-line-action (str "docker cp: " (white (str "from " (str-subs instance 0 12) ":" repos "/" from " to "  "."))))
      (print-command result)
      (lines-docker-error-instance result))))

(defn str-lines-docker-cp-pull [instance from]
  (apply str-join " " ["docker"
                       "cp"
                       (str instance ":" repos "/" from)
                       "."]))
      
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

(defn str-lines-docker-exec [job command instance]
  (apply str-join " " ["docker"
                       "exec"
                       "--tty"
                       "--interactive"
                       instance
                       (apply str-join " " (if (get job :entrypoint)
                                             (get job :entrypoint)
                                             ["sh" "-c"]))
                       "'" command "'"]))
      
(defn lines-docker-rm [instance]
  (let [result (docker ["rm"
                        "--force"
                        instance])]
    (do
      (output-line-action (str "docker rm: " (white (str-subs instance 0 12))))
      (print-command result))))

(defn str-lines-docker-instance-rm [instance]
  (apply str-join " " ["docker"
                       "rm"
                       "--force"
                       instance]))
      
(defn lines-docker-job-rm [instance services network]
  (do
    (lines-docker-rm instance)
    (map lines-docker-rm services)
    (lines-docker-network-rm network)))
      
(defn lines-job-docker-old [job]
  (let [network (lines-docker-network job)
        services (if (get job :services)
                    (do
                      (map (fn [service]
                            (lines-docker-run-service service network)) (get job :services))))
        instance (lines-docker-run job network)]
    (do
      (lines-docker-traps (concat [{:id instance :type "container"}]
                            (map (fn [item] {:id item :type "container"}) services)
                            [{:id network :type "network"}]))
      (lines-docker-cp-push instance current-path "/repos")
      ; (try*
      ;   (map (fn [code-line]
      ;         (do
      ;           (output-line-action (str "docker exec: " (white code-line)))
      ;           (lines-docker-exec! job instance code-line)))
      ;       (get job :script))
      ;   (catch* ex
      ;           (let [exit-code (last (str-split ex " "))]
      ;             (do
      ;               (println (bg-red (white (bold "JOB FAILED"))))))))

      (println (lines-task-loop job str-lines-docker-exec instance))

      (map (fn [path]
              (lines-docker-cp-pull instance path)) (get (get job :artifacts) :paths))
      (lines-docker-job-rm instance
                            services
                            network))))

(defn lines-job-docker [item]
  (let [instance (str "lines-" (time-ms))
        network-name (str-slug (str (get item :name) (time-ms)))
        services-names (if (get item :services) (map (fn [n] (str "lines-srv-" n "-" (time-ms))) (range 1 (count (get item :services)))))
        trap (lines-docker-traps (concat [{:id instance :type "container"}]
                                         (map (fn [item] {:id item :type "container"}) services-names)
                                         [{:id network-name :type "network"}]))
        services (if (get item :services)
                   (map
                    (fn [i] (str-lines-docker-run-service (nth (get item :services) i) (nth services-names i) network-name))
                    (range (- (count services-names) 1))))
        before-script (job {:name (str "before-script: " (get item :name))
                            :method "shell"
                            :script (concat [(str-lines-docker-network item network-name)]
                                            services
                                            [(str-lines-docker-run item instance network-name)]
                                            [(str-lines-docker-cp-push instance current-path "/repos")])})
        script (lines-task-loop item str-lines-docker-exec instance)
        after-script (job {:name (str "after-script: " (get item :name))
                           :method "shell"
                           :script (concat (map (fn [path]
                                                  (str-lines-docker-cp-pull instance path)) (get (get item :artifacts) :paths))
                                           [(str-lines-docker-instance-rm instance)]
                                           (if (get item :services)
                                             (map (fn [n] (str-lines-docker-instance-rm n)) services-names))
                                           [(str-lines-docker-network-rm network-name)])})]
    (concat (nth (get before-script :tasks) 0)
            script
            (nth (get after-script :tasks) 0))))