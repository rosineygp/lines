(def! template-block
  (fn* [k]
       (let* [begin "{{ "
              end " }}"]
             (str begin (key-name k) end))))

(defn template
  (fn* [text m]
       (reduce
        (fn* [a b]
             (str-replace a (template-block (first b)) (last b)))
        text
        (hashmap-list m))))

(defn str-template-command-line
  (fn* [item i]
       (let* [text (if (file-exists? (get i :src))
                     (slurp (get i :src))
                     (throw {:msg "file not exist"}))
              result (template text (get item :vars))]
             (if (isremote? item)
               (let* [temp-file (str temp-dir (str-slug (get item :name)) "-" (time-ms))]
                     (do
                       (spit temp-file result)
                       (job {:name "template scp copy"
                             :module "scp"
                             :target (get item :target)
                             :apply [{:src temp-file
                                      :dest (get i :dest)}]})
                       (unlink temp-file)))
               (spit (get i :dest) result)))))

(def! lines-module-template
  (fn* [item]
       (lines-task-loop item str-template-command-line)))