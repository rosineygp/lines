; spawn first function in repl (bug)

(defn command? [list]
  (map (fn [cmd]
         (let [exist? (nth (sh! (str "command -v " cmd)) 2)]
           (if (> exist? 0)
             (do
               (println-stderr "\033[31muse command failed: '" cmd "' not found!\033[0m")
               (throw (str "exit-code " exist?)) true)))) list))

(defmacro! use
  (fn* [list]
       (do
         (command? list)
         (map (fn* [cmd]
                   (quasiquote (defn ~cmd [args]
                      (if (vector? args)
                        (sh! (str ~cmd " " (apply str-join " " args)))
                        (sh! (str ~cmd)))))) list))))
                              
(defmacro! str-use
  (fn* [list]
       (map (fn [cmd]
              (quasiquote (defn ~cmd [args]
                            (if (vector? args)
                              (apply str-join " " ~cmd args)
                              (str ~cmd))))) list)))
