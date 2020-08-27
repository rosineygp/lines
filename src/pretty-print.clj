(defn timemillis-to-epoch [time]
  (/ (+ 500 time) 1000))

(defn timemillis-to-date [time]
  (nth (date ["--date"
              (str "@" (timemillis-to-epoch time))]) 0))

(defn lines-pp-cmd [script-line]
(let [std (get script-line :stdout)
        err (get script-line :stderr)
        exit-code (get script-line :exit-code)]
    (println (get script-line :raw-cmd))
    (if (= (empty? std) false) (println std))
    (if (= (empty? err) false) (println-stderr (red err)))
    (if (number? exit-code) (println-stderr (magenta exit-code)))))

(defn lines-pp-default [result]
  (println (get result :name))
  (println (timemillis-to-date (get result :start)))
  (map lines-pp-cmd (get result :script)))