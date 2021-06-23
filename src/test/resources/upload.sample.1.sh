#!/bin/bash

curl -v -F "file=@$PWD/sample.1.csv" http://localhost:8080/data-snapshots/upload
echo
