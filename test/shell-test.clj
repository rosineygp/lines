(load-file-without-hashbang "src/main.clj")

(job {:name "install curl local"
      :method "shell"
      :sudo true
      :script ["apt-get update"
               "apt-get install curl -y"]})

(job {:name "curl google"
      :method "shell"
      :script ["curl -s https://google.com"]})