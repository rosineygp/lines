(load-file-without-hashbang "src/main.clj")

(job {:name "test 1"
  :image "ubuntu"
  :method "docker"
  :entrypoint [
      "bash"
      "-c"
  ]
  :allow_failure false
  :script ["ls / | grep mnt"
           "if [[ true ]]; then echo ok; fi"
           "ps -ef"
           "ls /"]})