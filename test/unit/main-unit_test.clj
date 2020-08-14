(load-file-without-hashbang "src/test.clj")

(load-file-without-hashbang "src/includes/use.clj")
(load-file-without-hashbang "src/includes/colors.clj")
(load-file-without-hashbang "src/main.clj")

(use ["sleep"])

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

(deftest "branch-or-tag-name"
  (testing "local branch [dev]"
    (is (= (branch-or-tag-name) "dev")))
    
  (env "GITHUB_ACTIONS" "true")
  (env "GITHUB_REF" "/refs/heads/github-actions")

  (testing "github actions"
    (is (= (branch-or-tag-name) "github-actions")))
  
  (unset "GITHUB_ACTIONS")
  (unset "GITHUB_REF")

  (env "GITLAB_CI" "true")
  (env "CI_COMMIT_REF_NAME" "gitlab-ci")

  (testing "gitlab-ci"
    (is (= (branch-or-tag-name) "gitlab-ci")))

  (unset "GITLAB_CI")
  (unset "CI_COMMIT_REF_NAME")

  (env "JENKINS_URL" "http://jenkins")
  (env "GIT_BRANCH" "jenkins")
  
  (testing "jenkins"
    (is (= (branch-or-tag-name) "jenkins")))

  (unset "JENKINS_URL")
  (unset "GIT_BRANCH")

  (env "TRAVIS" "true")
  (env "TRAVIS_BRANCH" "travis")

  (testing "travis"
    (is (= (branch-or-tag-name) "travis")))

  (unset "TRAVIS")
  (unset "TRAVIS_BRANCH")

  (env "CIRCLECI" "true")
  (env "CIRCLE_TAG" "circle-ci-tag")
  (env "CIRCLE_BRANCH" "circle-ci-brach")

  (testing "circle-ci-tag"
    (is (= (branch-or-tag-name) "circle-ci-tag")))

  (unset "CIRCLE_TAG")

  (testing "circle-ci-branch"
    (is (= (branch-or-tag-name) "circle-ci-brach"))))