(use ["git"])

(def! str-cmd
  (fn* [args]
       (apply str-join " " args)))

(def! str-slug
  (fn* [string]
       (str-lower-case (reduce
                        (fn* [a b] (str-replace a b "-"))
                        string [":" "." "/" "_" " "]))))

(def! str-escapes
  (fn* [string]
       (reduce
        (fn* [a b] (str-replace a b (str "\\" b)))
        string ["'"])))

(def! branch-or-tag-name
  (fn* []
       (cond
         (= (env "GITHUB_ACTIONS") "true") (last (str-split (env "GITHUB_REF") "/"))
         (= (env "GITLAB_CI") "true") (env "CI_COMMIT_REF_NAME")
         (= (string? (env "JENKINS_URL")) true) (env "GIT_BRANCH")
         (= (env "TRAVIS") "true") (env "TRAVIS_BRANCH")
         (= (env "CIRCLECI") "true") (if (string? (env "CIRCLE_TAG")) (env "CIRCLE_TAG") (env "CIRCLE_BRANCH"))
         (= (nth (git ["rev-parse"
                       "--is-inside-work-tree"]) 0) "true") (nth (git ["rev-parse"
                                                                       "--abbrev-ref"
                                                                       "HEAD"]) 0))))

(def! isremote?
  (fn* [job]
       (let* [r (get-in job [:target :method])]
             (cond
               (nil? r) false
               (= r "local") false
               (keyword? :else) true))))

; job defaults
(def! lines-job-default-options
  (fn* []
       {:name "lines"
        :target {:method "local" :label "local"}
        :module "shell"
        :retries 0
        :ignore-error false
        :args {}}))

(def! lines-job-default-vars
  (fn* [item]
       (let* [b (branch-or-tag-name)
              a (if (get item :vars) (get item :vars) {})]
             (merge a
                    (hash-map "BRANCH_NAME" b
                              "BRANCH_NAME_SLUG" (str-slug b))))))

(def! lines-module-args
  (fn* [i]
       (let* [f (str "line-module-args-" (get i :module))
              a (if (get i :args) (get i :args) {})]
             (if (callable? f) ((call f) a) {}))))

(def! lines-module-vars
  (fn* [i]
       (let* [f (str "line-module-vars-" (get i :module))
              v (if (get i :vars) (get i :vars) {})]
             (if (callable? f) ((call f) v) {}))))

; job handler
(def! lines-job-status
  (fn* [l]
       (let* [exit-sum (reduce (fn* [a b] (+ a b)) 0 l)]
             (if (= exit-sum 0) true false))))

(def! lines-retries
  (fn* [r]
       (if (> r max-attempts) max-attempts r)))

(def! lines-job-retry-inner
  (fn* [retries k! f job]
       (let* [r ((call f) job)
              retry? (reduce (fn* [a b] (+ a b)) 0 (map (fn* [x] (get x :exit-code)) r))]
             (do
               (swap! k! concat [r])
               (if (> retry? 0)
                 (if (> retries 0)
                   (lines-job-retry-inner (dec retries) k! f job)))))))

(def! lines-job-retry
  (fn* [retries f job]
       (let* [k (atom [])]
             (do
               (lines-job-retry-inner retries k f job)
               @k))))

; task handler
(def! lines-task-execute
  (fn* [cmd user-cmd]
       (let* [start (time-ms)
              result (sh! cmd)
              finished (time-ms)]
             {:start start
              :finished finished
              :stdout (nth result 0)
              :stderr (nth result 1)
              :exit-code (nth result 2)
              :cmd user-cmd
              :debug cmd})))

(def! lines-task-loop
  (fn* [j f & more]
       (let* [l (get j :apply)
              t (count l)
              break (atom 0)]
             (filter (fn* [x] (map? x)) (map
                                         (fn* [i] (if (= @break 0)
                                                    (let* [user-cmd (nth l i)
                                                           str-cmd (apply f j user-cmd more)
                                                           r (lines-task-execute str-cmd user-cmd)]
                                                          (do
                                                            (if (> (get r :exit-code) 0) (swap! break inc))
                                                            r)) nil))
                                         (range t))))))

(def! job
  (fn* [item]
       (let* [common-vars (lines-job-default-vars item)
              common-opts (lines-job-default-options)
              module-vars (lines-module-vars item)
              module-args (lines-module-args item)
              item (let* [i (merge common-opts item)]
                         (assoc i
                                :vars (merge common-vars module-vars)
                                :args (merge module-args (get i :args))
                                :retries (lines-retries (get i :retries))))
              start (time-ms)
              result (let* [module (get item :module)]
                           (do
                             (if (not (or (= module "shell")
                                          (= module "docker")
                                          (= module "template")
                                          (= module "scp"))) (let* [f (str modules-dir module "/module.clj")]
                                                                   (if (file-exists? f)
                                                                     (load-once f)
                                                                     (throw (str "module: " module " not found.")))))
                             (lines-job-retry (get item :retries) (str "lines-module-" module) item)))
              pipestatus (map (fn* [l]
                                   (map (fn* [x] (get x :exit-code)) l)) result)
              status (lines-job-status (last pipestatus))
              bundle (assoc item
                            :attempts (count result)
                            :start start
                            :finished (time-ms)
                            :pipestatus pipestatus
                            :status status
                            :result result)]
             (if (or status (get item :ignore-error)) bundle (throw bundle)))))

(def! parallel
  (fn* [items]
       (let* [r (pmap (fn* [item] (try*
                                   (job item)
                                   (catch* ex ex))) items)
              e (reduce (fn* [a b] (and a b)) true (map (fn* [j] (or (get j :status) (get j :ignore-error))) r))]
             (if e r (throw r)))))

(def! filter-kv
  (fn* [p k]
       (filter (fn* [x] (not (empty? x)))
               (map (fn* [i] (if (sequential? i)
                               (filter-kv i k)
                               (if (not (empty? (filter (fn* [x] (= x (val k)))
                                                        (to-list (get i (key k)))))) i))) p))))

(def! merge-job-targets
  (fn* [j t]
       (if (sequential? j)
         (map (fn* [i] (merge-job-targets i t)) j)
         (map (fn* [i] (assoc j :target i)) t))))
                                                  
(def! pipeline
  (fn* [pipe]
       (let* [l (if (get pipe :filter-job)
                  (filter-kv (get pipe :jobs) (str-split-key-val (get pipe :filter-job)))
                  (get pipe :jobs))
              t (if (get pipe :filter-inventory)
                  (filter-kv (get pipe :inventory) (str-split-key-val (get pipe :filter-inventory)))
                  (get pipe :inventory))]
             (map (fn* [j] (cond
                             (and (sequential? j) (sequential? t)) (pmap parallel (merge-job-targets j t))
                             (sequential? t) (parallel (merge-job-targets j t))
                             (sequential? j) (parallel j)
                             (keyword? :else) (job j))) l))))