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

(job {:name "hello world"
      :method "shell"
      :script ["echo hello world"]})

(job {:name "show branch name"
      :method "shell"
      :script ["echo $BRANCH_NAME"]})

(job {:name "show current user"
      :method "shell"
      :script ["whoami"]})

(job {:name "show current user [sudo]"
      :method "shell"
      :sudo true
      :script ["whoami"]})

(job {:name "quotes"
      :method "shell"
      :script ["echo '$USER'"]})

(job {:name "variables"
      :method "shell"
      :sudo true
      :variables {
            rosiney "gomes pereira"
            my_var "${USER}${SHELL}"
      }
      :script ["printenv | sort"]})

(job {:name "change user"
      :method "shell"
      :user "nobody"
      :script ["whoami"]})

(job {:name "change user [not re-sudo]"
      :method "shell"
      :sudo true
      :user "nobody"
      :script ["whoami"]})

(job {:name "change entrypoint"
      :method "shell"
      :entrypoint ["sh" "-c"]
      :sudo true
      :user "nobody"
      :script ["whoami"]})