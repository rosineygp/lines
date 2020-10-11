(load-file-without-hashbang "src/main.clj")

(def j {:name "ignore-error"
        :ignore-error true
        :apply ["apt-get update"
                "apt-get upgrade -y"]})

(prn (job j))