(str-use ["scp"])

(def! str-scp-command-line
  (fn* [job i]
       (if (isremote? job)
         (let* [user (get-in job [:target :user])
                host (get-in job [:target :host])
                port (get-in job [:target :port])]
               (scp [(if (get i :recursive) "-r" "")
                     (get i :src)
                     (str user "@" host ":" (get i :dest))]))
         (throw {:msg "cannot do scp for local host"}))))

(def! lines-module-scp
  (fn* [job]
       (lines-task-loop job str-scp-command-line)))
