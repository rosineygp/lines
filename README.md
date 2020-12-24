sed remove empty lines<p align="center">
  <a alt="mkdkr" href="https://rosineygp.github.io/mkdkr">
    <img src="docs/assets/lines-transparent.png" width="246"/>
  </a>
</p>

# Lines

A pure bash clojureish CI pipeline.

Table of contents
-----------------

* [Usage](#usage)
  * [Installation](#installation)
  * [Job keywords](#job-keywords)

# Usage

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
curl https://github.com/rosineygp/lines-sh/releases/download/v0.92.10/lines > lines
chmod + x lines
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

| keyword                       | value   | description                                                |
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
| template | render **lines** template and copy to it to destiny          |
| scp      | copy files over scp                                          |

> is possible create custom modules

### target

Host target is the location where the job will run. If any target passed the job will run at localhost.

```edn
{:target {:label "web-server" 
          :host "web.local.net" 
          :port 22 
          :method "ssh"}}
```
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
{:name "install curl"
 :args {:sudo true}
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

> The max retries are **2**, but it can be increase setting **LINES_JOB_MAX_ATTEMPTS** at enviroment vars.

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