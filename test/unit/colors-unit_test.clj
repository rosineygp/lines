(load-file-without-hashbang "src/test.clj")
(load-file-without-hashbang "src/includes/colors.clj")

(deftest "colors"
  (testing "testing color gray"
    (is (= (gray "COLOR") "\\033[30mCOLOR\\033[0m")))

  (testing "testing color red"
    (is (= (red "COLOR") "\\033[31mCOLOR\\033[0m")))

  (testing "testing color green"
    (is (= (green "COLOR") "\\033[32mCOLOR\\033[0m")))

  (testing "testing color yellow"
    (is (= (yellow "COLOR") "\\033[33mCOLOR\\033[0m")))

  (testing "testing color blue"
    (is (= (blue "COLOR") "\\033[34mCOLOR\\033[0m")))

  (testing "testing color magenta"
    (is (= (magenta "COLOR") "\\033[35mCOLOR\\033[0m")))

  (testing "testing color cyan"
    (is (= (cyan "COLOR") "\\033[36mCOLOR\\033[0m")))

  (testing "testing color white"
    (is (= (white "COLOR") "\\033[37mCOLOR\\033[0m"))))

(deftest "bg-colors"
    (testing "testing bg-color gray"
        (is (= (bg-gray "COLOR") "\\033[40mCOLOR\\033[0m")))
    
    (testing "testing bg-colors red"
        (is (= (bg-red "COLOR") "\\033[41mCOLOR\\033[0m")))
    
    (testing "testing bg-colors green"
        (is (= (bg-green "COLOR") "\\033[42mCOLOR\\033[0m")))
    
    (testing "testing bg-colors yellow"
        (is (= (bg-yellow "COLOR") "\\033[43mCOLOR\\033[0m")))
    
    (testing "testing bg-colors blue"
        (is (= (bg-blue "COLOR") "\\033[44mCOLOR\\033[0m")))
    
    (testing "testing bg-colors magenta"
        (is (= (bg-magenta "COLOR") "\\033[45mCOLOR\\033[0m")))
    
    (testing "testing bg-colors cyan"
        (is (= (bg-cyan "COLOR") "\\033[46mCOLOR\\033[0m")))
    
    (testing "testing bg-colors white"
        (is (= (bg-white "COLOR") "\\033[47mCOLOR\\033[0m"))))

(deftest "attributes"
    (testing "testing attribute bold"
        (is (= (bold "COLOR") "\\033[1mCOLOR\\033[0m")))
    
    (testing "testing attribute dark"
        (is (= (dark "COLOR") "\\033[2mCOLOR\\033[0m")))
    
    (testing "testing attribute underline"
        (is (= (underline "COLOR") "\\033[4mCOLOR\\033[0m")))
    
    (testing "testing attribute blink"
        (is (= (blink "COLOR") "\\033[5mCOLOR\\033[0m")))
    
    (testing "testing attribute reverse-color"
        (is (= (reverse-color "COLOR") "\\033[7mCOLOR\\033[0m")))
    
    (testing "testing attribute concealed"
        (is (= (concealed "COLOR") "\\033[8mCOLOR\\033[0m"))))