(def j {:name "ssh"
        :target {:host "192.168.1.12"
                 :user "root"
                 :method "ssh"}
        :ignore-error true
        :apply ["apt update && apt upgrade -y"]})

(lines-pp (job j))