#!/bin/bash

set -e

echo "patching flk"

_command_exist() {
  local -r cmd="${1}"
  if ! [[ -x $(command -v "${cmd}") ]]; then
    echo -e "\ncommand: $cmd not found, please install it.\n"
    exit 1
  fi
}

_command_exist "patch"
_command_exist "curl"

flk_url="https://raw.githubusercontent.com/chr15m/flk/beabb477daac370f5fa52eabdec7dc977b944198/docs/flk"

set -x
curl "${flk_url}" > .flk

for i in patches/*.flk.patch; do
  patch -i "$i" -u .flk
done  

chmod +x .flk

mv .flk flk