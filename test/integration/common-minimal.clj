(load-file-without-hashbang "src/main.clj")

(def j {:apply ["echo welcome to lines"]})

(prn (job j))