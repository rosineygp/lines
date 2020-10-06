#!/usr/bin/env bash

set -xe

./flk "${PWD}/test/unit/use-unit_test.clj"
./flk "${PWD}/test/unit/colors-unit_test.clj"
./flk "${PWD}/test/unit/core-unit_test.clj"
