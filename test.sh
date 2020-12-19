#!/usr/bin/env bash

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

test="all"

if [ "${1}" != "" ]; then
  test="${1}"
fi

if [ "${test}" == "all" ] || [ "${test}" == "unit" ];then
  set -e
  echo "unit test"
  _test_folder "./flk" "${PWD}/test/unit/" 
fi

if [ "${test}" == "all" ] || [ "${test}" == "integration" ];then
  echo "integration test"
  _test_folder "./lines" "${PWD}/test/integration/" "-c"
fi

if [ "${test}" == "all" ] || [ "${test}" == "edn" ];then
  echo "test edn"
  _test_folder "./lines" "${PWD}/test/edn/" "-p"
fi
