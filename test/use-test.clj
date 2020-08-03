(load-file-without-hashbang "src/includes/use.clj")

(use ["ls" "pwd" "ps"])

; simple commands
(println (ls))
(println (pwd))
(println (ps))

; commands with args
(println (ls ["-la"]))
(println (ls ["-la"
              "/tmp"]))
(println (ps ["xua"]))
