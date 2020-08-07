(load-file-without-hashbang "src/main.clj")

(job {:name "test 1"
      :image "alpine"
      :method "docker"
      :allow_failure false
      :services [{:alias "nginx"
                  :image "nginx:latest"}
                  {:image "redis"}]
      :script ["ls -la"
               "echo ola"
               "sleep 60"]})