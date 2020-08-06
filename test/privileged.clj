(load-file-without-hashbang "src/main.clj")

(job {:name "test 1"
      :image "docker:19"
      :method "docker"
      :privileged true
      :script ["echo mount docker"
               "docker ps -a"
               "docker run -ti --rm --name ubuntei ubuntu cat /etc/hostname"]})