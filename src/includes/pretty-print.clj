(use ["date"])

(def! timemillis-to-epoch
  (fn* [time]
       (/ (+ 500 time) 1000)))

(def! timemillis-to-date
  (fn* [time]
       (nth (date ["--date"
                   (str "@" (timemillis-to-epoch time))]) 0)))

(def! lines-pp-hr
  (fn* [n s]
       (apply str (map (fn* [] (str s)) (range n)))))

(def! lines-pp-cmd-command
  (fn* [line]
       (str (green "  cmd: ") (get line :cmd))))

(def! lines-pp-cmd-stdout
  (fn* [line]
       (if (= (empty? (get line :stdout)) false) (str (str-indent (get line :stdout) 4) "\n") "")))

(def! lines-pp-cmd-stderr
  (fn* [line]
       (if (= (empty? (get line :stderr)) false) (str (str-indent (red (get line :stderr))) "\n") "")))

(def! lines-pp-cmd-exit-code
  (fn* [line]
       (str (red "  exit-code:") " " (get line :exit-code))))

(def! lines-pp-cmd-time
  (fn* [line]
       (str (blue "  time:") " " (- (get line :finished) (get line :start)))))

(def! lines-pp-cmd
  (fn* [line]
       (str (lines-pp-cmd-command line) "\n"
            (lines-pp-cmd-stderr line)
            (lines-pp-cmd-stdout line)
            (lines-pp-cmd-exit-code line))))

(def! lines-pp-script
  (fn* [result]
       (apply str-join "\n" (map lines-pp-cmd result))))

(def! lines-pp-name
  (fn* [result]
       (str (magenta "name:") " " (get result :name))))

(def! lines-pp-target-label
  (fn* [label]
       (str (magenta "target:") " " label)))

(def! lines-pp-title
  (fn* [title]
       (let* [size (count (seq title))]
             (str (magenta "name:") " " title " " (magenta (lines-pp-hr (- 80 (+ size 8)) "*"))))))

(def! lines-pp-start
  (fn* [time]
       (str (cyan "start:") " " (timemillis-to-date time))))

(def! lines-pp-finished
  (fn* [time]
       (let* [date-time (timemillis-to-date time)
              size (count (seq date-time))]
             (str (cyan "finished:") " " date-time " " (cyan (lines-pp-hr (- 80 (+ size 12)) "*"))))))

(def! lines-pp-total
  (fn* [result]
       (str (blue "total:") " " (- (get result :finished) (get result :start)) " ms")))

(def! lines-pp-pipestatus
  (fn* [result]
       (str (red "pipestatus:") " " (apply str-join " " (get result :pipestatus)))))

(def! lines-pp-status
  (fn* [result]
       (str (magenta "status:") " " (get result :status))))

(def! str-lines-pp
  (fn* [i]
       (if (sequential? i)
         (do
           (apply str-join "\n" (concat [(bold (magenta (str "parallel: " (count i) " { ")))]
                                        (pmap str-lines-pp i)
                                        [(bold (magenta "}"))])))
         (do
           (apply str-join "\n" (concat [(lines-pp-title (get i :name))
                                         (lines-pp-target-label (or (get-in i [:target :label]) (get-in i [:target :host])))
                                         (lines-pp-start (get i :start))]
                                        (pmap (fn* [l]
                                                   (lines-pp-script l)) (get i :result))
                                        [(lines-pp-status (get i :status))
                                         (lines-pp-finished (get i :finished))]))))))

(def! lines-pp
  (fn* [l]
       (map (fn* [i] (println (str-lines-pp i))) (to-list l)) l))

(def! lines-pp-minimal
  (fn* [l]
       (map (fn* [i]
                 (if (sequential? i)
                   (lines-pp-minimal i)
                   (println
                    (blue ":job") (get i :name)
                    (magenta ":target") (or (get-in i [:target :label]) (get-in i [:target :host]))
                    (green ":status") (get i :status)))) (to-list l)) l))