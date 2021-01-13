; spawn first function in repl (bug)

(defn command? [cmd]
  (let [exist? (nth (sh! (str "command -v " cmd)) 2)]
    (if (> exist? 0)
      (do
        (println-stderr "\033[31muse command failed: '" cmd "' not found!\033[0m")
        (throw {:exit-code exist?}) true))))

(defn use [i]
  (let [l (if (sequential? i) i (list i))]
    (do
      (map command? l)
      (map (fn [cmd] (eval (quasiquote
                            (defn ~(symbol cmd) [args]
                              (if (sequential? args)
                                (sh! (str ~cmd " " (apply str-join " " args)))
                                (sh! (str ~cmd))))))) l))))

(defn str-use [i]
  (let [l (if (sequential? i) i (list i))]
    (do
      (map command? l)
      (map (fn [cmd]
             (eval (quasiquote (defn ~(symbol cmd) [args]
                                 (if (sequential? args)
                                   (apply str-join " " ~cmd args)
                                   (str ~cmd)))))) l))))
