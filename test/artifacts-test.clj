(load-file-without-hashbang "src/main.clj")

(job {:name "test 1"
      :image "alpine"
      :method "docker"
      :allow_failure true
      :artifacts {:paths ["file"
                          "tt"]}
      :script ["ls -la > file"
               "mkdir tt"
               "touch tt/1"
               "touch tt/2"
               "cat file"]})