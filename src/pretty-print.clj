(defn timemillis-to-epoch [time]
  (/ (+ 500 time) 1000))

(defn timemillis-to-date [time]
  (nth (date ["--date"
              (str "@" (timemillis-to-epoch time))]) 0))

; ugly
(defn lines-pp-hr [n s]
  (apply str (map (fn [] (str s)) (range n))))

(defn lines-pp-cmd-command [line]
  (str (green "  cmd:") 
    (if (= (str-subs (get line :cmd) 0 4) "sudo") " # " " $ ")
    (get line :raw-cmd)))

(defn lines-pp-cmd-stdout [line]
  (if (= (empty? (get line :stdout)) false) (str (str-indent (get line :stdout) 4) "\n") ""))

(defn lines-pp-cmd-stderr [line]
  (if (= (empty? (get line :stderr)) false) (str (str-indent (red (get line :stderr))) "\n") ""))

(defn lines-pp-cmd-exit-code [line]
  (str (red "  exit-code:") " " (get line :exit-code)))

(defn lines-pp-cmd-time [line]
  (str (blue "  time:") " " (- (get line :finished) (get line :start))))

(defn lines-pp-cmd [line]
  (str (lines-pp-cmd-command line) "\n"
       (lines-pp-cmd-stderr line)
       (lines-pp-cmd-stdout line)
       (lines-pp-cmd-exit-code line)))

(defn lines-pp-script [result]
  (apply str-join "\n" (map lines-pp-cmd (get result :script))))

(defn lines-pp-name [result]
  (str (magenta "name:") " " (get result :name)))

(defn lines-pp-title [result]
  (let [size (count (seq (get result :name)))]
    (str (magenta "name:") " " (get result :name) " " (magenta (lines-pp-hr (- 80 (+ size 8)) "*")))))

(defn lines-pp-start [result]
  (str (cyan "start:") " " (timemillis-to-date (get result :start))))

(defn lines-pp-finished [result]
  (let [date-time (timemillis-to-date (get result :finished))
        size (count (seq date-time))]
    (str (cyan "finished:") " " date-time " " (cyan (lines-pp-hr (- 80 (+ size 12)) "*")))))

(defn lines-pp-total [result]
  (str (blue "total:") " " (- (get result :finished) (get result :start)) " ms"))

(defn lines-pp-pipestatus [result]
  (str (red "pipestatus:") " " (apply str-join " " (get result :pipestatus))))

(defn lines-pp-status [result]
  (str (magenta "status:") " " (get result :status)))

(defn lines-pp [result]
  (println
   (str (lines-pp-title result) "\n"
        (lines-pp-start result) "\n"
        (lines-pp-script result) "\n"
        (lines-pp-status result) "\n"
        (lines-pp-finished result) "\n")))