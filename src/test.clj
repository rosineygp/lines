; super small framework for unit test

(def asserts (atom 0))
(def exit-code (atom 0))

(defn deftest [name f]
  (let [start (time-ms)]
    (do
      f
      (if (= @exit-code 0)
        (do
          (println (str (green (str "[ " name  " ]")) 
            " asserts: " @asserts "/" (- @asserts @exit-code) 
            ", time elapsed: " (- (time-ms) start) "ms\n"))
          (reset! asserts 0))
        (do
          (println (str (red (str "[ " name  " ]")) 
            " asserts: " @asserts "/" (- @asserts @exit-code) 
            ", time elapsed: " (- (time-ms) start) "ms\n"))
          (exit! @exit-code))))))

(defn testing [msg f]
  (do
    (swap! asserts inc)
    (println (if f
                (do
                  (green "[   ok   ]"))
                (do
                  (swap! exit-code inc)
                  (red "[ failed ]"))) msg)))

(defn is [comparison]
  (= comparison true))
