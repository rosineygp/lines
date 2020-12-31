<p align="center">
  <a alt="mkdkr" href="https://rosineygp.github.io/mkdkr">
    <img src="docs/assets/logo.png"/>
  </a>
</p>

# Lines

A pure bash clojureish CI pipeline.

# Main Features

* Complete CI engine
* Execute local or remote
* Pure data syntax `edn`
* Clojure script syntax
* Easy command line integration like (docker, kubectl, apt)
* Concurrency execution
* Modular and extensible

# Why?

* Alternative method to install clojure, using "clojure", but without clojure
* Tired to write `yaml` every day
* Create something cool with [Fleck](https://github.com/chr15m/flk)

Table of contents
-----------------

* [Installation](#installation)
* [Job keywords](#job-keywords)
* [Modules](#modules)
  * [shell](#shell)
  * [docker](#docker)
  * [scp](#scp)
  * [template](#template)
  * [user module](#user-module)
* [EDN Pipeline](#edn-pipeline)
  * [Targets file](#targets-file)
* [Clojure Pipeline](#clojure-pipeline)
  * [Lines functions](#lines-functions)
  * [Environment vars](#enviroment-vars)
* [Extensions](#extensions)
* [Development](#development)

## Installation

### requirements

* bash 4
* docker ¹
* ssh ²
* scp ²

> ¹ docker module<br>² remote execution

### install script

```bash
# download

curl -L https://github.com/rosineygp/lines-sh/releases/latest/download/lines > lines
chmod +x lines
sudo mv lines /usr/bin/lines
```

### minimal usage

Create a file `.lines.edn` with followin data.

```edn
[{:apply ["echo hello world!"]}]
```

Just execute command `lines`

```shell
name: lines *******************************************************************
target: local
start: ter 22 dez 2020 21:38:01 -03
  cmd: echo hello world!
    hello world!
  exit-code: 0
status: true
finished: ter 22 dez 2020 21:38:04 -03 ****************************************
```

## Job keywords

A job is defined as a hashmap of keywords. The commons keywords available for job are:

| keyword                       | type    | description                                                |
|-------------------------------|---------|------------------------------------------------------------|
| [apply](#apply)               | array   | the tasks that job will handler                            |
| [name](#name)                 | string  | optional job name                                          |
| [module](#module)             | string  | module name                                                |
| [target](#target)             | hashmap | where the job will run                                     |
| [vars](#vars)                 | hashmap | set env vars (branch name changes conforme current branch) |
| [args](#args)                 | hashmap | arguments modules, each module has own args                |
| [ignore-error](#ignore-error) | boolean | when job fail the execution continue                       |
| [retries](#retries)           | integer | number of retries if job fail, max 2                       |

### apply

Is the only required keyword that job needs. It's an array of objects that will be executed by a module. Each element of array is a tasks and the job can handler N tasks. If any tasks has a exit code different than 0, the job will stop and throw the error. **ignore-error** and **retries** helps to handler errors.

```edn
{:apply ["uname -a"
         "make build"]}
```

> using default module **shell**, apply is a array of strings.

```edn
{:module "scp"
 :apply [{:src "program.tar.gz" :dest "/tmp/program.tar.gz"}
         {:src "script.sh"      :dest "/tmp/script.sh"}]}
```

> **scp** uses a list of hashmaps

### name

**name** is just a label for the job.

```edn
{:name "install curl"
 :module "shell"
 :apply ["apk add curl"]}
```

### module

**module** is the method executed by job (default is **shell**)

```edn
{:module "shell"
 :apply ["whoami"]}
```

Docker module

```edn
{:module "docker"
 :args {:image "node"}
 :apply ["npm test"]}
```

the builtin modules are:

| module   | description                                                  |
|----------|--------------------------------------------------------------|
| shell    | execute shell commands                                       |
| docker   | start a docker instance and execute shell commands inside it |
| template | render **lines template** and copy to it to destiny          |
| scp      | copy files over scp                                          |

> is possible create custom modules

### target

Host **target** is the location where the job will run. If any target passed the job will run at localhost.

```edn
{:target {:label "web-server"
          :host "web.local.net"
          :port 22
          :method "ssh"}}
```
> Targets can be defined in separated file, during execution is possible to merge data with job and execute the same job in n hosts.

| keywords | description                       |
|----------|-----------------------------------|
| label    | host label, just for identify     |
| host     | ip or fqdn for access host        |
| user     | login user                        |
| port     | method port 22 is default         |
| method   | connection method, ssh is default |

> Is possible set another keywords for filter like **group**, **dc** or any other value you need to organizer targets.

After job executed it return themself with result values

### vars

Variables will be inject in environment during tasks execution.

```edn
{:vars {MY_VAR_0 "lines"
        MY_VAR_1 "go"}
 :apply ["echo $MY_VAR_0"
         "echo $MY_VAR_1"]}
```

> **BRANCH_NAME** and **BRANCH_NAME_SLUG** are inject in environment.

### args

Args is the parameters of modules.

```edn
{:args {:sudo true}
 :apply ["apt-get update"
         "apt-get install htop -y]}
```

> All tasks will run with **sudo**

### ignore-error

If some task fail, lines will not stop the pipeline, just return the current task failed.

```edn
{:ignore-error true
 :apply ["whoami"
         "exit 1"
         "dpkg -l"]}
```

> The tasks after error will not be executed.

### retries

If some task fail, retry will run it again.

```edn
{:retries 2
 :apply ["ping -c 1 my-host"]}
```

> The max retries are **2**, but it can be increase setting **LINES_JOB_MAX_ATTEMPTS** at environment vars.

## Modules

### shell

Is it the default module, just spawn scripts to shell.

```edn
{:module "shell"
 :apply ["date"]}
```

#### arguments

| keyword    | type    | description                                    |
|------------|---------|------------------------------------------------|
| sudo¹      | boolean | apply commands using sudo                      |
| user¹      | string  | change current user                            |
| entrypoint | array   | change initial entry command (default is bash) |

> ¹ needs pre configured sudoers (without password)

### docker

Create a docker instance and execute commands inside it.

#### single instance

```edn
{:module "docker"
 :apply ["whoami"]}
```

* start docker instance with default image (alpine)
* run command **whoami** inside container

#### services

```edn
{:module "docker"
 :args {:image "ubuntu"
        :services [{:image "nginx"
                    :alias "nginx"}]}
 :apply ["apt-get update"
         "apt-get install curl -y"
         "curl http://nginx"]}
```

* start docker instance with nginx image as a **service** and set network alias as **nginx**
* start another docker instance with ubuntu image
* install ubuntu packages
* execute curl at service from ubuntu instance

### download artifacts

Download files or directory from a docker instance.

```edn
{:module "docker"
 :args {:artifacts {:paths ["file"
                            "directory"]}}
 :apply ["touch file"
         "mkdir directory"
         "touch directory/file"]}
```

#### arguments

| keyword                             | type    | description                                            |
|-------------------------------------|---------|--------------------------------------------------------|
| image                               | string  | docker instance path name                              |
| entrypoint                          | array   | change initial entry command (default is sh)           |
| privileged                          | boolean | run job with privileged access and mount docker socket |
| [services](#services-description)   | array   | services description                                   |
| [artifacts](#artifacts-description) | hasmap  | download artifact from docker instance                 |

#### services description

Services is an array of hashmaps, is possible up N services with docker module, the following keywords can be used to build a service.

| keyword    | type    | description                                       |
|------------|---------|---------------------------------------------------|
| image      | string  | docker instance path name                         |
| vars       | hashmap | like job vars but exclusive from instance service |
| alias      | string  | network alias name, otherwise slug image name     |
| entrypoint | string  | service entrypoint string, otherwise ''           |

#### artifacts description

Download a files or directories from a docker instance.

| keyword | type  | description                          |
|---------|-------|--------------------------------------|
| paths   | array | file or folder relative or full path |

### scp

Copy files and folders to remote host over **scp**.

```edn
{:module "scp"
 :apply [{:src "./dist/command.bin"
          :dest "/usb/bin/command"}]}
```

#### apply arguments

| keyword   | type    | description                     |
|-----------|---------|---------------------------------|
| src       | string  | file or directory source        |
| dest      | string  | file or directory destiny       |
| recursive | boolean | set **true** for directory copy |

### template

Simple template file that only replace values inside double brackets `{{ varname }}`.

```jinja
Hello {{ NAME }}!
```

> template file

```edn
{:module "template"
 :vars {NAME "lines"}
 :apply [{:src "./hello-world.j2"
          :dest "/tmp/hello-world.txt"}]}
```

### user module

Lines provides interface with custom user module.

Just put additional clojure scripts at `.lines/modules/<module_name>/module.clj`, like following example.

```clj
; .lines/modules/git/module.clj

; create a boilerplate function for git command
(str-use ["git"])

; custom user function, params: job (receive job definition), i (apply index)
; the function to return a string eg. `git clone -v git@github.com:rosineygp/mkdkr.git mkdkr`
(defn str-git-command-line [job i]
  (git ["clone"
        "-v"
        (get i :repos)
        (get i :dest)]))

; lines will call this function `lines-module-<module_name>`
(defn lines-module-git [job]
  (lines-task-loop job str-git-command-line)) ; loop handler
```

Using user module.

```edn
{:module "git"
 :apply [{:repos "git@github.com:rosineygp/lines.git" :dest "lines"}
         {:repos "git@github.com:rosineygp/mkdkr.git" :dest "mkdkr"}]}
```

## EDN Pipeline

Pipelines is an array of jobs, and can be described using only edn.

```edn
; file: node.edn

[{:name "build"
  :apply ["npm install"]}
 [{:name "unit test"
   :group ["test"]
   :apply ["npm unit"]}
  {:name "mocha test"
   :group ["test"]
   :apply ["npm mocha"]}]
 {:name "deploy"
  :apply ["npm deploy"]}]
```

Jobs inside pipeline can be executed in parallel, just group then with `[ array ]`, is possible use custom keywords for better notation like `:groups`.

```shell
# execute all jobs
lines -p node.edn

# filter only tests
lines -p node.edn -j group=test
```

### Targets file

Is possible describe hosts targets using `edn` files, like pipeline.

```edn
; file: hosts.edn

[{:label "vm-0" :host "192.168.1.4" :method "ssh" :user "ubuntu"}
 {:label "vm-1" :host "192.168.1.5" :method "ssh" :user "ubuntu"}]
```

> ssh only works with `authorized_keys` pre-configured, command `ssh-copy-id` can help configure it.

```shell
# execute pipeline in all hosts
lines -p node.edn -i hosts.edn

# filter only wm-0 host
lines -p node.edn -i hosts.edn -l label=wm-0

# filter jobs deploy and host wm-1
lines -p node.edn -i hosts.edn -l label=wm-1 -j name=deploy
```

> EDN files are not safe is possible call functions dynamically.

```edn
; unsafe pipeline, extensions are based on this behavior

[{:apply [(str "echo " (time-ms))]}]
```

## Clojure Pipeline

Powerful and dynamic pipelines with complex scenarios.

```clojure
; generate single job for each yml found and test with docker image yamllint

(lines-pp (parallel
    (map (fn [x] (assoc {}
                        :name (get x :object)
                        :module "docker"
                        :args {:image "my/yamllint"}
                        :apply [(str "yamllint " (get x :object))]))
            (filter (fn [i] (= "yml" (get i :type))) (list-dir "src/")))))
```

> This project use this case to test itself at `.lines.clj`.

### Lines functions

The following functions will help to build lines clojure scripts.

| name              | description                                              |
|-------------------|----------------------------------------------------------|
| job               | execute edn job                                          |
| parallel          | execute jobs in parallel                                 |
| str-use           | mapping command line to function, return str not execute |
| use               | mapping command line to function, run immediately        |
| pipeline          | execute end pipeline                                     |
| lines-pp          | beautiful print pipeline                                 |
| lines-pp-minimal  | beautiful print pipeline (single line)                   |
| merge-job-targets | generate a mixed list with edn pipelines and targets     |
| isremote?         | check if target will run local or in remote machine      |

`src/core.clj` and `src/includes/lang-utils.clj` has more useful functions.

Additional functions included in Lines.

filter | pmap | println_stderr | exit! | trap! | unset | str-join | str-subs | range | str-ident | mod | file-exists | file-write | unlink | or | and | hashmap-list | merge | key-name | call | callable? | get-in | even? | odd? | load-once | spit

The result of function `job` is the following data:

```edn
({:attempts 1
  :args {}
  :module "shell"
  :status true
  :apply ["echo hello world!"]
  :name "lines"
  :retries 0
  :target {:label "local" :method "local"}
  :pipestatus ((0))
  :finished 1608684590507
  :vars {"BRANCH_NAME_SLUG" "master" "BRANCH_NAME" "master"}
  :ignore-error false
  :start 1608684587657
  :result (({:exit-code 0
             :finished 1608684590253
             :cmd "echo hello world!"
             :stdout "hello world!"
             :stderr ""
             :start 1608684590233
             :debug "  bash -c $' export BRANCH_NAME_SLUG=\"master\" BRANCH_NAME=\"master\" ; echo hello world! ' "}))})
```

### Environment vars

Lines variables definition.

| name                   | default           | description                                      |
|------------------------|-------------------|--------------------------------------------------|
| LINES_JOB_TTL          | 3600              | max time that a job can run (only docker module) |
| LINES_JOB_MAX_ATTEMPTS | 2                 | max number of retries                            |
| LINES_MODULES_DIR      | ".lines/modules/" | modules default location                         |
| LINES_EXT_DIR          | ".lines/ext/"     | extensions default location                      |
| FLK_MAX_THREADS        | undefined         | max number of threads in parallel execution      |

## Extensions

Is possible create functions and extend usability, all extensions must be keep at `.lines/ext/`, it is just a clojure script with useful functions.

```edn
; file: .lines/ext/apt-helper.clj

(str-use ["apt-get"])

(defn apt-update []
  (apt-get ["update"]))

(defn apt-args [options packages]
   (apt-get  (concat options packages)))

(defn apt-install [packages]
  (apt-args ["install" "-y"] packages))

(defn apt-remove [packages]
  (apt-args ["remove" "-y"] packages))
```

Now edn files or pipeline can call those functions.

```edn
; edn pipeline

[{:name "apt with extensions"
  :apply [(apt-update)
          (apt-install ["python"
                        "mysql-server"
                        "memcached"])]}]
```

## Development

Build this project

```shell
./build.sh

> result
  flk lines
```

`flk` is same of [Fleck](https://github.com/chr15m/flk) with `patches/` applied and `lines` is flk with all `src/` included.

Testing changes in `src/` without build.

```shell
./flk src/main.clj <params>
```