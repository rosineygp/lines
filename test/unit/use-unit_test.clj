(load-file-without-hashbang "src/test.clj")
(load-file-without-hashbang "src/includes/use.clj")

(use ["ls"])

(deftest "use"
  (testing "assignment command"
    (is (= (fn? ls) true)))

  (testing "calling command, exit code"
    (is (= (nth (ls ["/"]) 2) 0)))

  (testing "calling command, stdout"
    (is (= (string? (nth (ls ["/"]) 0)) true)))

  (testing "calling command, strerr"
    (is (= (string? (nth (ls ["/"]) 0)) true)))

  (testing "assignment nonexistent command"
    (try*
     (use ["fake_command"])
     (catch* e (is (= e {:exit-code 1}))))))