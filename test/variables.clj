(load-file-without-hashbang "src/main.clj")

(job {:name "printenv"
      :image "alpine"
      :method "docker"
      :variables {MY_VAR "teste"
                  MY_VALUES 1
                  DATE (nth (date) 0)}
      :allow_failure false
      :script ["printenv"]})