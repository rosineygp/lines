(load-file-without-hashbang "src/main.clj")

(job {:name "test 1"
      :image "alpine"
      :method "docker"
      :allow_failure false
      :services [{:alias "web1"
                  :image "nginx:latest"}
                  {:image "nginx"}]
      :script ["apk add curl --no-cache"
               "curl nginx"
               "curl web1"]})