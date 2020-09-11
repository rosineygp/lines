(defn template-block [k]
  (let [begin "{{ "
        end " }}"]
    (str begin (key-name k) end)))

(defn template [text m]
  (reduce
   (fn [a b]
     (str-replace a (template-block (first b)) (last b)))
   text
   (hashmap-list m)))