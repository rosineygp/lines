(str-use ["docker"])

(defn lines-docker-traps [list]
  (trap! (str "{ " (apply str-join "; " (map (fn [item]
                                               (if (= (get item :type) "container")
                                                 (str "docker rm -f " (get item :id))
                                                 (str "docker network rm " (get item :id)))) list)) "; }") "EXIT"))

(defn str-lines-docker-network [network-name]
  (docker ["network"
           "create"
           network-name]))

(defn str-lines-docker-network-rm [network-name]
  (docker ["network"
           "rm"
           network-name]))

(defn str-lines-docker-run [job instance network]
  (docker ["run"
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
           (get-in job [:args :image])
           "sleep" ttl]))

(defn str-lines-docker-run-service [service instance network]
  (docker ["run"
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
  (docker ["cp"
           from
           (str instance ":" to)]))

(defn str-lines-docker-cp-pull [instance from]
  (docker ["cp"
           (str instance ":" repos "/" from)
           "."]))

(defn str-lines-docker-exec [job command instance]
  (docker ["exec"
           "--interactive"
           instance
           (apply str-join " " (if (get job :entrypoint)
                                 (get job :entrypoint)
                                 ["sh" "-c"]))
           "'" command "'"]))

(defn str-lines-docker-instance-rm [instance]
  (docker ["rm"
           "--force"
           instance]))

(defn line-module-args-docker [args]
  (let [d {:image "alpine"}]
    (merge d args)))

(defn lines-module-docker [item]
  (let [instance (str (str-slug (get item :name)) "-" (time-ms))
        remote? (isremote? item)
        services-list (get-in item [:args :services])
        network-name instance
        services-names (if services-list (map (fn [n] (str "srv-" n "-" instance)) (range (count services-list))))
        trap (lines-docker-traps (concat [{:id instance :type "container"}]
                                         (map (fn [item] {:id item :type "container"}) services-names)
                                         [{:id network-name :type "network"}]))
        services (if services-list (map
                                    (fn [i] (str-lines-docker-run-service (nth services-list i) (nth services-names i) network-name))
                                    (range (count services-names))))
        upload-files (if remote? (job {:name (str "upload-files: " (get item :name))
                                       :target (get item :target)
                                       :module "scp"
                                       :apply [{:src current-path :recursive true :dest (str "/tmp/" instance)}]}))
        before-script (job {:name (str "before-script: " (get item :name))
                            :module "shell"
                            :target (get item :target)
                            :apply (concat [(str-lines-docker-network network-name)]
                                           services
                                           [(str-lines-docker-run item instance network-name)]
                                           [(str-lines-docker-cp-push instance (if remote? (str "/tmp/" instance "/.") (str current-path "/.")) (str repos "/"))])})
        script (lines-task-loop (assoc item
                                       :apply (map (fn [line]
                                                     (str-lines-docker-exec item line instance)) (get item :apply))) str-shell-command-line)
        after-script (job {:name (str "after-script: " (get item :name))
                           :module "shell"
                           :target (get item :target)
                           :apply (concat (map (fn [path]
                                                 (str-lines-docker-cp-pull instance path)) (get-in item [:args :artifacts :paths]))
                                          [(str-lines-docker-instance-rm instance)]
                                          (if services-list (map (fn [n] (str-lines-docker-instance-rm n)) services-names))
                                          [(str-lines-docker-network-rm network-name)])})]
    (concat (if remote? (nth (get upload-files :result) 0))
            (nth (get before-script :result) 0)
            script
            (nth (get after-script :result) 0))))