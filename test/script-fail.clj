(load-file-without-hashbang "src/main.clj")

(job {:name "allow failure: true"
      :image "alpine"
      :method "docker"
      :allow_failure true
      :script ["ls / | grep mnt"
               "exit 1"
               "apk add --no-cache curl"
               "pwd"
               "ps -ef"
               "ls /"]})

(job {:name "allow failure: unset"
      :image "alpine"
      :method "docker"
      :script ["ls / | grep mnt"
               "exit 1"
               "apk add --no-cache curl"
               "pwd"
               "ps -ef"
               "ls /"]})

(job {:name "allow failure: false"
      :image "alpine"
      :method "docker"
      :allow_failure false
      :script ["ls / | grep mnt"
               "exit 1"
               "apk add --no-cache curl"
               "pwd"
               "ps -ef"
               "ls /"]})