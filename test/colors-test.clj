(load-file-without-hashbang "src/includes/colors.clj")

; foreground
(println (gray "gray foreground"))
(println (red "red foreground"))
(println (green "green foreground"))
(println (yellow "yellow foreground"))
(println (blue "blue foreground"))
(println (magenta "magenta foreground"))
(println (cyan "cyan foreground"))

; background
(println (bg-gray "gray background"))
(println (bg-red "red background"))
(println (bg-green "green background"))
(println (bg-yellow "yellow background"))
(println (bg-blue "blue background"))
(println (bg-magenta "magenta background"))
(println (bg-cyan "cyan background"))

; attributes
(println (bold "bold"))
(println (dark "dark"))
(println (underline "underline"))
(println (blink "blink"))
(println (reverse-color "reverse-color"))
(println (concealed "concealed"))

; complex
(println (bg-red 
  (bold 
    (white "DANGER:"))) "a danger message")

(println (str 
          (gray "gray") 
          (red "red")
          (green "green")
          (yellow "yellow")
          (blue "blue")
          (magenta "magenta")
          (cyan "cyan")))
