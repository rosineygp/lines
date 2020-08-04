(load-file-without-hashbang "src/main.clj")

(job {:name "test 1"
  :image "alpine"
  :method "docker"
  :allow_failure false
  :script ["ls / | grep mnt"
           "exit 1"
           "apk add --no-cache curl"
           "pwd"
           "ps -ef"
           "ls /"]})