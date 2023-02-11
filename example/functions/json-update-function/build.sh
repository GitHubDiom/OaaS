#!/usr/bin/env bash

tag=0.2.15
base=core.harbor.10.131.36.2.nip.io/oaas/
name=json-update-function
image=$base$name:$tag

docker build -t $image .
docker push $image
