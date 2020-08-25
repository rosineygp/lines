#!/bin/bash

set -e

_command_exist() {
  local -r cmd="${1}"
  if ! [[ -x $(command -v "${cmd}") ]]; then
    echo -e "\ncommand: $cmd not found, please install it.\n"
    exit 1
  fi
}

_command_exist "patch"
_command_exist "curl"

set -x
curl https://chr15m.github.io/flk/flk > .flk

patch -i patches/001-println.flk.patch -u .flk
patch -i patches/002-join.flk.patch -u .flk
patch -i patches/003-pmap.flk.patch -u .flk
patch -i patches/004-exit.flk.patch -u .flk
patch -i patches/005-trap.flk.patch -u .flk
patch -i patches/006-str_join.flk.patch -u .flk
patch -i patches/007-str_subs.flk.patch -u .flk
patch -i patches/009-unset.flk.patch -u .flk
patch -i patches/010-pmap.flk.patch -u .flk
patch -i patches/011-pmap.flk.patch -u .flk

chmod +x .flk
mv .flk flk