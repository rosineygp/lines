#!/usr/bin/env bash

set -e

if [ ! -f ./flk ]; then
  echo "building...."
  ./build.sh
fi

_test_folder () {
  local o=""
  local -r c="${1}"; shift
  local -r p="${1}"; shift
  [ "${1}" != "" ] && o="${1}"

  for f in "${p}"*; do
    echo "${f}"
    if [ "${o}" != "" ]; then
      "${c}" "${o}" "${f}"
    else
      "${c}" "${f}"
    fi
  done
}

echo "unit test"
_test_folder "./flk" "${PWD}/test/unit/" 

exit 0

echo "integration test"
_test_folder "./lines" "${PWD}/test/integration/" "-c"

echo "test edn"
_test_folder "./lines" "${PWD}/test/edn/" "-p"
