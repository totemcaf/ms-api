#!/bin/bash

scp -i $PEM \
  ../target/scala-2.12/minesweeper-assembly-0.1.0-SNAPSHOT.jar \
  ubuntu@ec2-3-87-195-146.compute-1.amazonaws.com:/home/ubuntu/api/minesweeper.jar

scp -i $PEM \
  Dockerfile \
  ubuntu@ec2-3-87-195-146.compute-1.amazonaws.com:/home/ubuntu/api

scp -i $PEM \
  runOnDocker.sh \
  ubuntu@ec2-3-87-195-146.compute-1.amazonaws.com:/home/ubuntu/api

ssh  -i $PEM ubuntu@ec2-3-87-195-146.compute-1.amazonaws.com "cd api && bash runOnDocker.sh"
