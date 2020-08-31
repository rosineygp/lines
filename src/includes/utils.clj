(defn or [& i]
  (let [r (atom 0)]
    (do
      (map (fn [x]
             (if (= x true) (swap! r inc) false)) i)
      (if (> @r 0) true false))))