(load-file-without-hashbang "src/includes/use.clj")
(load-file-without-hashbang "src/includes/colors.clj")
(load-file-without-hashbang "src/core.clj")
(load-file-without-hashbang "src/docker.clj")

(use ["pwd"
      "pwd"
      "date"
      "git"])

(def current-path (nth (pwd) 0))
(def current-dir (last (str-split current-path "/")))
(def repos (str "/repos/" current-dir))

(def ttl 3600)

(defn job [item]
  (do
    (output-line-banner (str "begin: " (get item :name)))
    (if (= (get item :method) "docker")
      (do
        (lines-docker-job item)))
    (output-line-banner (str "done: " (get item :name)))))

(defn parallel [items]
  (pmap (fn [item] (job item)) items))
