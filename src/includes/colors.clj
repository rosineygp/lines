(defn colors-escape-code
  [i]
  (str "\033[" i "m"))

(defn colors-reset [] (colors-escape-code 0))

(def colors-code {gray    30
                  red     31
                  green   32
                  yellow  33
                  blue    34
                  magenta 35
                  cyan    36
                  white   37
                  bg-gray    40
                  bg-red     41
                  bg-green   42
                  bg-yellow  43
                  bg-blue    44
                  bg-magenta 45
                  bg-cyan    46
                  bg-white   47
                  bold          1
                  dark          2
                  underline     4
                  blink         5
                  reverse-color 7
                  concealed     8})

(defn colors-make [l]
  (map (fn [color]
         (eval (quasiquote (defn ~(symbol color) [string]
                             (str
                              (colors-escape-code (get colors-code ~color))
                              string
                              (colors-reset)))))) l))

(colors-make ["gray"
              "red"
              "green"
              "yellow"
              "blue"
              "magenta"
              "cyan"
              "white"
              "bg-gray"
              "bg-red"
              "bg-green"
              "bg-yellow"
              "bg-blue"
              "bg-magenta"
              "bg-cyan"
              "bg-white"
              "bold"
              "dark"
              "underline"
              "blink"
              "reverse-color"
              "concealed"])
