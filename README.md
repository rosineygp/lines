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

## Job

Job data model description.

```end
{:name "lines"
 :module "shell"
 :target {}
 :vars {}
 :args {}
 :ignore-error false
 :retries 0
 :apply []}
```

|keyword|value|default|description|
|-------|-----|-------|-----------|
|:name|string|`lines`|optional job name|
|:module|string|`shell`|module name|
|:target|hashmap|`{:label "local":method "local"}`|where the job will run|
|:vars|hashmap|`{BRANCH_NAME "master" BRANCH_NAME_SLUG "master"}`|set env vars (branch name changes conforme current branch)|
|:args|hashmap|`{}`|arguments modules, each module has own args|
|:ignore-error|boolean|`false`|when job fail the execution continue|
|:retries|integer|`0`|number of retries if job fail, max 2|
|:apply|array|`[]`|the tasks that job will handler|

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