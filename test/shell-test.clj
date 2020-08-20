(load-file-without-hashbang "src/main.clj")

(job {:name "install curl local"
      :method "shell"
      :allow_failure true
      :retries 2
      :sudo true
      :script ["echo $BRANCH_NAME"
               "apt-get update"
               "apt-get install curl -y"]})

(job {:name "curl google"
      :method "shell"
      :script ["curl -s https://google.com"]})