(load-file-without-hashbang "src/main.clj")

(def j {:name "sudo test"
        :args {:sudo true}
        :apply ["apt-get update"
                "apt-get upgrade -y"]})

(prn (job j))