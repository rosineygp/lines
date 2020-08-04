(load-file-without-hashbang "src/includes/use.clj")
(load-file-without-hashbang "src/includes/colors.clj")

(use ["pwd"
      "docker"
      "pwd"
      "date"])

(def current-path (nth (pwd) 0))
(def current-dir (last (str-split current-path "/")))
(def repos (str "/repos/" current-dir))

(def ttl 3600)

(defn str-date-time []
  (nth (date ["+'%Y-%m-%d %T'"]) 0))

(defn output-line-action [action]
  (println-stderr (green action)))

(defn output-line-banner [name]
  (println-stderr (str (bg-blue (bold (white (str  name " > " (str-date-time))))))))

(defn print-command [result]
  (let [std (nth result 0)
        err (nth result 1)
        exit-code (nth result 2)]
    (if (= (empty? std) false) (println std))
    (if (= (empty? err) false) (println-stderr (red err)))
    (if (number? exit-code) (println-stderr (magenta exit-code)))))

(defn lines-error-handler [job instance exit-code]
  (if (> exit-code 0)
    (if (= (get job :allow_failure) false)
      (do
        (lines-docker-rm instance)
        (exit! exit-code)))))

(defn lines-docker-run [job]
  (let [result (docker ["run"
                        "--detach"
                        "--rm"
                        "--workdir" repos
                        (get job :image)
                        "sleep" ttl])]
    (do
      (output-line-action (str "docker run: " (get job :image)))
      (print-command result)
      (if (> (nth result 2) 0)
        (exit! (nth result 2)))
      (nth result 0))))

(defn lines-docker-exec [job instance command]
  (let [result   (docker ["exec"
                          "--tty"
                          "--interactive"
                          instance
                          "sh" "-c '" command "'"])]
    (do
      (output-line-action (str "docker exec: " command))
      (print-command result)
      (lines-error-handler job instance (nth result 2)))))

(defn lines-docker-rm [instance]
  (let [result (docker ["rm"
                        "--force"
                        instance])]
    (do
      (output-line-action (str "docker rm: " instance))
      (print-command result))))

(defn lines-docker-job [job]
  (let [instance (lines-docker-run job)]
    (do
      (map (fn* [code-line]
                (lines-docker-exec job instance code-line))
           (get job :script))
      (lines-docker-rm instance))))

(defn job [item]
  (do
    (output-line-banner (str "begin: " (get item :name)))
    (if (= (get item :method) "docker")
      (do
        (lines-docker-job item)))

    (output-line-banner (str "done: " (get item :name)))))

(defn parallel [items]
  (pmap (fn* [item] (job item)) items))

; user area
; simple job
(job {:name "test 1"
      :image "alpine"
      :method "docker"
      :allow_failure true
      :script ["ls / | grep mnt"
               "exit 1"
               "apk add --no-cache curl"
               "pwd"
               "ps -ef"
               "ls /"]})

; parallel job

(def p-jobs [{:name "job1"
              :image "alpine"
              :method "docker"
              :script ["apk add --no-cache curl"
                       "pwd"
                       "ps -ef"
                       "ls"]}
             {:name "job2"
              :image "alpine"
              :method "docker"
              :script ["apk add --no-cache curl"
                       "pwd"
                       "ps -ef"
                       "ls"]}])

; (parallel p-jobs)
