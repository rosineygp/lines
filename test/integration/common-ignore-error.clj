(def j {:name "ignore-error"
        :ignore-error true
        :apply ["echo error"
                "exit 1"]})

(lines-pp (job j))