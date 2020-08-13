(load-file-without-hashbang "src/test.clj")

(load-file-without-hashbang "src/includes/use.clj")
(load-file-without-hashbang "src/includes/colors.clj")
(load-file-without-hashbang "src/main.clj")

(deftest "str-date-time"
  (testing "return type"
    (is (= (string? (str-date-time)) true)))

  (testing "time format (lenght)"
    (is (= (count (seq (str-date-time))) 19))))

(deftest "str-slug"
  (testing "to lower case"
    (is (= (str-slug "UPPERCASE") "uppercase")))

  (testing "replace :"
    (is (= (str-slug "c:l:e:a:n") "c-l-e-a-n")))

  (testing "replace ."
    (is (= (str-slug "c.l.e.a.n") "c-l-e-a-n")))

  (testing "replace /"
    (is (= (str-slug "c/l/e/a/n") "c-l-e-a-n")))

  (testing "replace _"
    (is (= (str-slug "c_l_e_a_n") "c-l-e-a-n")))

  (testing "replace spaces"
    (is (= (str-slug "c l e a n") "c-l-e-a-n"))))