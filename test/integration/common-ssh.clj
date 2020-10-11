(load-file-without-hashbang "src/main.clj")

(def j {:name "ssh"
        :target {:host "192.168.1.12"
                 :user "root"
                 :method "ssh"}
        :apply ["apt update && apt upgrade -y"]})

(prn (job j))