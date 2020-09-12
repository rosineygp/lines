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
  
(defn lines-docker-cp-pull [instance from]
  (let [result (docker ["cp"
                        (str instance ":" repos "/" from)
                        "."])]
    (do
      (output-line-action (str "docker cp: " (white (str "from " (str-subs instance 0 12) ":" repos "/" from " to "  "."))))
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
      (lines-docker-traps (concat [{:id instance :type "container"}]
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

      (map (fn [path]
              (lines-docker-cp-pull instance path)) (get (get job :artifacts) :paths))
      (lines-docker-job-rm instance
                            services
                            network))))