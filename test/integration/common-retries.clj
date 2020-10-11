(load-file-without-hashbang "src/main.clj")

(def j {:name "retries"
        :retries 2
        :apply ["apt-get update"
                "apt-get upgrade -y"]})

(prn (job j))