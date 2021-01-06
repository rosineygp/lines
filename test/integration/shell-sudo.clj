(def j {:name "sudo test"
        :args {:sudo true}
        :apply ["apt-get update"]})

(lines-pp (job j))