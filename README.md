# Lines

A pure bash clojureish CI pipeline.

## requirements

- docker
- git
- bash >= 4 [flk](https://github.com/chr15m/flk)

## write a job

A job is defined in a `ci.clj` file

```clojure
; simple job
(job {:name "first job"
      :image "alpine"
      :method "docker"
      :script ["apk add --no-cache curl"
               "curl https://google.com"]})

; simple service
(job {:name "service"
      :image "alpine"
      :method "docker"
      :services [{:alias "web"
                  :image "nginx:latest"}
                 {:image "nginx"}]
      :script ["apk add curl --no-cache"
               "curl nginx"
               "curl web"]})

; privileged job
(job {:name "docker in docker"
      :image "docker:19"
      :method "docker"
      :privileged true
      :script ["docker ps -a"
               "docker image inspect docker:19"]})

; enviroment variables
(job {:name "printenv"
      :image "alpine"
      :method "docker"
      :variables {MY_VAR "teste"
                  MY_VALUES 1
                  DATE (nth (date) 0)}
      :script ["printenv"]})

; artifacts
(job {:name "save files"
      :image "alpine"
      :method "docker"
      :artifacts {:paths ["file"
                          "tt"]}
      :script ["ls -la > file"
               "mkdir tt"
               "touch tt/1"
               "touch tt/2"]})
```

## job definition edn

```edn
{:name "string"
 :image "string"
 :method "docker"
 :artifacts {
    :paths ["string"
            "string"]}
 :privileged boolean
 :allow_failure boolean
 :entrypoint ["string"
              "string"]
 :services [{:alias "string"
             :image "string"
             :entrypoint "string"}]
 :script ["string"
          "string"
          "string"]}
```

## build

```bash
./flk ci.clj
```

> generate dist/lines

## run dev

At root of project

```bash
./flk src/main.clj
```

## test

```bash
./flk test/job.clj
./flk test/parallel.clj
./flk test/pmap-test.clj
./flk test/use-test.clj
./flk test/variables.clj
```