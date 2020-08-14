; super small framework for unit test

(def asserts (atom 0))
(def exit-code (atom 0))

(defn deftest [name f]
  (let [start (time-ms)]
    (do
      f
      (if (= @exit-code 0)
        (do
          (println (str "\033[32m[ " name  " ]\033[0m")
                   " asserts: " @asserts "/" (- @asserts @exit-code) "\n")
          (reset! asserts 0))
        (do
          (println (str "\033[31m[ " name  " ]\033[0m")
                   " asserts: " @asserts "/" (- @asserts @exit-code) "\n")
          (exit! @exit-code))))))

(defn testing [msg f]
  (do
    (swap! asserts inc)
    (println (if f
               (do
                 (str "\033[32m[   ok   ]\033[0m"))
               (do
                 (swap! exit-code inc)
                 (str "\033[31m[ failed ]\033[0m"))) msg)))

(defn is [comparison]
  (= comparison true))
