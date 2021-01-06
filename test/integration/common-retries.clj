(def j {:name "retries"
        :retries 2
        :ignore-error true
        :apply ["apt-get update"]})

(lines-pp (job j))