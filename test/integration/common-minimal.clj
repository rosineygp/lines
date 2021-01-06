(def j {
  :retries 2
  :apply ["echo welcome to lines"]})

(lines-pp (job j))