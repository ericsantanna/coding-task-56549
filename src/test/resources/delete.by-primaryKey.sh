#!/bin/bash

curl -kv -X DELETE "http://localhost:8080/data-snapshots/$1"
echo
