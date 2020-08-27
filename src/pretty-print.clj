(defn timemillis-to-epoch [time]
  (/ (+ 500 time) 1000))

(defn timemillis-to-date [time]
  (nth (date ["--date"
              (str "@" (timemillis-to-epoch time))]) 0))

; ugly
(defn lines-pp-hr []
  (str "--------------------------------------------------------------------------------\n"))

(defn lines-pp-cmd [line]
  (str (green "cmd:") " " (get line :raw-cmd) "\n"
       (if (= (empty? (get line :stderr)) false) (str (red (get line :stderr) "\n")) "")
       (if (= (empty? (get line :stdout)) false) (str (get line :stdout) "\n") "")
       (str (red "exit-code:") " " (get line :exit-code) "\n"
            (blue "time:") " " (- (get line :finished) (get line :start)) " ms\n")))

(defn lines-pp-default [result]
  (println
   (str (lines-pp-hr)
        (magenta "job:") " " (get result :name) "\n"
        (cyan "start:") " " (timemillis-to-date (get result :start)) "\n\n"
        (apply str-join "\n" (map lines-pp-cmd (get result :script))) "\n"
        (cyan "finished:") " " (timemillis-to-date (get result :finished)) "\n"
        (blue "total:") " " (- (get result :finished) (get result :start)) " ms\n"
        (red "pipestatus:") " " (apply str-join " " (get result :pipestatus)) "\n"
        (magenta "status:") " " (get result :status))))