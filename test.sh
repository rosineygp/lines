#!/usr/bin/env bash

if [ ! -f ./flk ]; then
  echo "building...."
  ./build.sh
fi

set -xe

echo "unit test"
./flk "${PWD}/test/unit/use-unit_test.clj"
./flk "${PWD}/test/unit/colors-unit_test.clj"
./flk "${PWD}/test/unit/core-unit_test.clj"