(use ["git"])

(defn str-cmd [args]
  (apply str-join " " args))

(defn str-slug [string]
  (str-lower-case (reduce
                   (fn [a b] (str-replace a b "-"))
                   string [":" "." "/" "_" " "])))

(defn str-escapes [string]
  (reduce
   (fn [a b] (str-replace a b (str "\\" b)))
   string ["'"]))

(defn branch-or-tag-name []
  (cond
    (= (env "GITHUB_ACTIONS") "true") (last (str-split (env "GITHUB_REF") "/"))
    (= (env "GITLAB_CI") "true") (env "CI_COMMIT_REF_NAME")
    (= (string? (env "JENKINS_URL")) true) (env "GIT_BRANCH")
    (= (env "TRAVIS") "true") (env "TRAVIS_BRANCH")
    (= (env "CIRCLECI") "true") (if (string? (env "CIRCLE_TAG")) (env "CIRCLE_TAG") (env "CIRCLE_BRANCH"))
    (= (nth (git ["rev-parse"
                  "--is-inside-work-tree"]) 0) "true") (nth (git ["rev-parse"
                                                                  "--abbrev-ref"
                                                                  "HEAD"]) 0)))

(defn isremote? [job]
  (let [r (get-in job [:target :method])]
    (cond
      (nil? r) false
      (= r "local") false
      (keyword? :else) true)))

; job defaults
(defn lines-job-default-options []
  {:name "lines"
   :stage "default"
   :target {:method "local"}
   :module "shell"
   :retries 0
   :ignore-error false
   :args {}})

(defn lines-job-default-vars [item]
  (let [b (branch-or-tag-name)
        a (if (get item :vars) (get item :vars) {})]
    (merge a
           (hash-map "BRANCH_NAME" b
                     "BRANCH_NAME_SLUG" (str-slug b)))))

(defn lines-module-args [i]
  (let [f (str "line-module-args-" (get i :module))
        a (if (get i :args) (get i :args) {})]
    (if (callable? f) ((call f) a) {})))

(defn lines-module-vars [i]
  (let [f (str "line-module-vars-" (get i :module))
        v (if (get i :vars) (get i :vars) {})]
    (if (callable? f) ((call f) v) {})))

; job handler
(defn lines-job-status [l]
  (let [exit-sum (reduce (fn [a b] (+ a b)) 0 l)]
    (if (= exit-sum 0) true false)))

(defn lines-retries [r]
  (if (> r max-attempts) max-attempts r))

(defn lines-job-retry-inner [retries k! f job]
  (let [r ((call f) job)
        retry? (reduce (fn [a b] (+ a b)) 0 (map (fn [x] (get x :exit-code)) r))]
    (do
      (swap! k! concat [r])
      (if (> retry? 0)
        (if (> retries 0)
          (lines-job-retry-inner (dec retries) k! f job))))))

(defn lines-job-retry [retries f job]
  (let [k (atom [])]
    (do
      (lines-job-retry-inner retries k f job)
      @k)))

; task handler
(defn lines-task-execute [cmd user-cmd]
  (let [start (time-ms)
        result (sh! cmd)
        finished (time-ms)]
    {:start start
     :finished finished
     :stdout (nth result 0)
     :stderr (nth result 1)
     :exit-code (nth result 2)
     :cmd user-cmd
     :debug cmd}))

(defn lines-task-loop [j f & more]
  (let [l (get j :apply)
        t (count l)
        break (atom 0)]
    (filter (fn [x] (map? x)) (map
                               (fn [i] (if (= @break 0)
                                         (let [user-cmd (nth l i)
                                               str-cmd (apply f j user-cmd more)
                                               r (lines-task-execute str-cmd user-cmd)]
                                           (if (> (get r :exit-code) 0) (swap! break inc))
                                           r) nil))
                               (range t)))))

(defn job [item]
  (let [common-vars (lines-job-default-vars item)
        common-opts (lines-job-default-options)
        module-vars (lines-module-vars item)
        module-args (lines-module-args item)
        item (let [i (merge common-opts item)]
               (assoc i 
                      :vars (merge common-vars module-vars)
                      :args (merge module-args (get i :args))
                      :retries (lines-retries (get i :retries))))
        start (time-ms)
        result (let [module (get item :module)]
                 (do
                   (if (not (or (= module "shell")
                                (= module "docker")
                                (= module "scp"))) (load-once (str "src/modules/" module ".clj")))
                   (lines-job-retry (get item :retries) (str "lines-module-" module) item)))
        pipestatus (map (fn [l]
                          (map (fn [x] (get x :exit-code)) l)) result)
        status (lines-job-status (last pipestatus))
        bundle (assoc item
                      :attempts (count result)
                      :start start
                      :finished (time-ms)
                      :pipestatus pipestatus
                      :status status
                      :result result)]
    (if (or status (get item :ignore-error)) bundle (throw bundle))))

(defn parallel [items]
  (let [r (pmap (fn [item] (try*
                            (job item)
                            (catch* ex ex))) items)
        e (reduce (fn [a b] (and a b)) true (map (fn [j] (get j :status)) r))]
    (if e r (throw r))))

(defn pipeline [p]
  (let [r (map job (read-string (slurp p)))]
    (do
      (lines-pp r)
      r)))