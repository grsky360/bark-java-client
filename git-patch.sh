#!/usr/bin/env sh

BASE=origin/$(git rev-parse --abbrev-ref HEAD)
if [ $# = 1 ]; then
    BASE=$1
fi

git format-patch HEAD..."${BASE}" --stdout > git.patch
