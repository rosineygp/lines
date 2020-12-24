sed remove empty lines<p align="center">
  <a alt="mkdkr" href="https://rosineygp.github.io/mkdkr">
    <img src="docs/assets/lines-transparent.png" width="246"/>
  </a>
</p>

# Lines

A pure bash clojureish CI pipeline.

> ¹ only if using docker module

Table of contents
-----------------

* [Usage](#usage)
  * [Installation](#installation)


# Usage

## Installation

### requirements

- bash 4
- docker¹
- ssh²
- scp²

> ¹docker module, ²remote execution

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

A job is defined as a hzshmap of keywords that define the job’s behavior.

The common keywords available for jobs are:

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

**apply** is an array of objects, the data passed will change with diferents modules.

```edn
{:module "shell"
 :apply ["uname -a"
         "make build"]}
```

> using **shell** module apply is a array of strings.

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
{:apply ["whoami"]}
```

```edn
 :module "docker"
 :args {:image "node"}
 :apply ["npm test"]}
```

the builtin modules are

| module   | description                                                  |
|----------|--------------------------------------------------------------|
| shell    | execute shell commands                                       |
| docker   | start a docker instance and execute shell commands inside it |
| template | render **lines** template and copy to it to destiny          |
| scp      | copy files over scp                                          |

> is possible crate custom modules

After job executed it return themself with result values

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