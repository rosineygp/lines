(load-file-without-hashbang "src/test.clj")

(load-file-without-hashbang "src/includes/use.clj")
(load-file-without-hashbang "src/includes/colors.clj")
(load-file-without-hashbang "src/main.clj")


(deftest "str-date-time"
  (testing "return type"
    (is (= (string? (str-date-time)) true)))

  (testing "time format (lenght)"
    (is (= (count (seq (str-date-time))) 19))))