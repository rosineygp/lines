(def test (or (get (str-split-keys-values (get args :arguments) " ") :test) "all"))

(defn test-folder [command folder ignore-error]
  (do
    (println "folder: " folder)
    (parallel (map (fn [x] (assoc {}
                                  :name (get x :object)
                                  :ignore-error ignore-error
                                  :apply [(str command " " (get x :object))])) (list-dir folder)))))

(if (or (= test "all") (= test "unit"))
  (lines-pp (test-folder "./flk" "test/unit/" false)))
(if (or (= test "all") (= test "integration"))
  (lines-pp (test-folder "./lines -c" "test/integration/" false)))
(if (or (= test "all") (= test "edn"))
  (lines-pp (test-folder "./lines -p" "test/edn/" true)))