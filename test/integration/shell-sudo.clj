(def j {:name "sudo test"
        :args {:sudo true}
        :apply ["apt-get update"
                "apt-get upgrade -y"]})

(prn (job j))