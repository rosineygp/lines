(load-file-without-hashbang "src/main.clj")

(def connetion {:host "192.168.1.153"
                :port 22
                :user "root"
                :password ""})

(job {:name "install curl local"
      :method "ssh"
      :connection connetion
      :script ["apt-get update"
               "apt-get install curl -y"]})

(job {:name "install docker"
      :method "ssh"
      :connection connetion
      :script ["curl https://get.docker.com | sh"]})

(job {:name "start docker service"
      :method "ssh"
      :connection connetion
      :script ["systemctl start docker"]})

(job {:name "test docker"
      :method "ssh"
      :connection connetion
      :script ["docker run hello-world"]})