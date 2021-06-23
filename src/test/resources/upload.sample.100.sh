#!/bin/bash

curl -kv -F "file=@$PWD/sample.100.csv" http://localhost:8080/data-snapshots/upload
echo
