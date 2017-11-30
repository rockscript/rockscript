#!/usr/bin/env bash

mvn -Pizza clean install

if [ $? -eq 0 ]; then
    THISDIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
    $THISDIR/run-server.sh
fi
