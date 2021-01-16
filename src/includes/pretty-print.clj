(use ["date"])

(defn timemillis-to-epoch [time]
  (/ (+ 500 time) 1000))

(defn timemillis-to-date [time]
  (nth (date ["--date"
              (str "@" (timemillis-to-epoch time))]) 0))

(defn lines-pp-hr [n s]
  (apply str (map (fn [] (str s)) (range n))))

(defn lines-pp-cmd-command [line]
  (str (green "  cmd: ") (get line :cmd)))

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
   (apply str-join "\n" (map lines-pp-cmd result)))

(defn lines-pp-name [result]
  (str (magenta "name:") " " (get result :name)))

(defn lines-pp-target-label [label]
    (str (magenta "target:") " " label))

(defn lines-pp-title [title]
  (let [size (count (seq title))]
    (str (magenta "name:") " " title " " (magenta (lines-pp-hr (- 80 (+ size 8)) "*")))))

(defn lines-pp-start [time]
  (str (cyan "start:") " " (timemillis-to-date time)))

(defn lines-pp-finished [time]
  (let [date-time (timemillis-to-date time)
        size (count (seq date-time))]
    (str (cyan "finished:") " " date-time " " (cyan (lines-pp-hr (- 80 (+ size 12)) "*")))))

(defn lines-pp-total [result]
  (str (blue "total:") " " (- (get result :finished) (get result :start)) " ms"))

(defn lines-pp-pipestatus [result]
  (str (red "pipestatus:") " " (apply str-join " " (get result :pipestatus))))

(defn lines-pp-status [result]
  (str (magenta "status:") " " (get result :status)))

(defn str-lines-pp [i]
  (if (sequential? i)
    (do
      (apply str-join "\n" (concat [(bold (magenta (str "parallel: " (count i) " { ")))]
                                   (pmap str-lines-pp i)
                                   [(bold (magenta "}"))])))
    (do
      (apply str-join "\n" (concat [(lines-pp-title (get i :name))
                                    (lines-pp-target-label (or (get-in i [:target :label]) (get-in i [:target :host])))
                                    (lines-pp-start (get i :start))]
                                   (pmap (fn [l]
                                           (lines-pp-script l)) (get i :result))
                                   [(lines-pp-status (get i :status))
                                    (lines-pp-finished (get i :finished))])))))

(defn lines-pp [l]
   (map (fn [i] (println (str-lines-pp i)))  l) l)


(defn lines-pp-minimal [l]
  (map (fn [i]
         (if (sequential? i)
           (lines-pp-minimal i)
           (println
            (blue ":job") (get i :name)
            (magenta ":target") (or (get-in i [:target :label]) (get-in i [:target :host]))
            (green ":status") (get i :status)))) (if (sequential? l) l (list l))) l)