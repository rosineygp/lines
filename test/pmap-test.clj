(load-file-without-hashbang "src/includes/use.clj")

(use ["sleep" "date"])

(defn timer [d]
  (do
    (println "sleep:" d)
    (println "begin:" d (date))
    (sleep [d])
    (println "end:" d (date))))


(pmap (fn [x] (timer x)) [10 10 10])
(pmap (fn [x] (timer x)) [5 1 10])