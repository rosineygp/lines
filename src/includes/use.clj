; spawn first function in repl (bug)

(defn command? [list]
  (map (fn [cmd]
         (let [exist? (nth (sh! (str "command -v " cmd)) 2)]
           (if (> exist? 0)
             (do
               (println "use command failed: '" cmd "' not found!")
               (exit! exist?)) true))) list))

(defmacro! use
  (fn* [list]
       (do
         (command? list)
         (map (fn* [cmd]
                   `(defn ~cmd [args]
                      (if (vector? args)
                        (sh! (str ~cmd " " (apply str-join " " args)))
                        (sh! (str ~cmd))))) list))))
