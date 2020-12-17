(def j {:name "retries"
        :retries 2
        :apply ["apt-get update"
                "apt-get upgrade -y"]})

(prn (job j))