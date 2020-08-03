; spawn first function in repol (bug)

(defmacro! use
  (fn* [list]
       (map (fn* [cmd]
                `(defn ~cmd [args]
                  (if (vector? args)
                      (sh! (str ~cmd " " (apply join " " args)))
                      (sh! (str ~cmd))))) list)))
