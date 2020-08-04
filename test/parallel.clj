(load-file-without-hashbang "src/main.clj")

(parallel [{:name "job1"
  :image "alpine"
  :method "docker"
  :script ["apk add --no-cache curl"
           "pwd"
           "ps -ef"
           "ls"]}
 {:name "job2"
  :image "alpine"
  :method "docker"
  :script ["apk add --no-cache curl"
           "pwd"
           "ps -ef"
           "ls"]}])
