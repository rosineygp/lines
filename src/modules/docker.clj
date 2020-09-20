(defn lines-docker-traps [list]
  (trap! (str "{ " (apply str-join "; " (map (fn [item]
                                               (if (= (get item :type) "container")
                                                 (str "docker rm -f " (get item :id))
                                                 (str "docker network rm " (get item :id)))) list)) "; }") "EXIT"))

(defn str-lines-docker-network [job network-name]
  (apply str-join " "  ["docker"
                        "network"
                        "create"
                        network-name]))

(defn str-lines-docker-network-rm [network-name]
  (apply str-join " " ["docker"
                       "network"
                       "rm"
                       network-name]))

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

(defn str-lines-docker-cp-push [instance from to]
  (apply str-join " " ["docker"
                       "cp"
                       from
                       (str instance ":" to)]))

(defn str-lines-docker-cp-pull [instance from]
  (apply str-join " " ["docker"
                       "cp"
                       (str instance ":" repos "/" from)
                       "."]))

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

(defn str-lines-docker-instance-rm [instance]
  (apply str-join " " ["docker"
                       "rm"
                       "--force"
                       instance]))

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