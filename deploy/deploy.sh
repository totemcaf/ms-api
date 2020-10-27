#!/bin/bash

scp -i $PEM \
  ../target/scala-2.12/minesweeper-assembly-0.1.0-SNAPSHOT.jar \
  ubuntu@ec2-3-87-195-146.compute-1.amazonaws.com:/home/ubuntu/minesweeper.jar

scp -i $PEM \
  Dockerfile \
  ubuntu@ec2-3-87-195-146.compute-1.amazonaws.com:/home/ubuntu

scp -i $PEM \
  runOnDocker.sh \
  ubuntu@ec2-3-87-195-146.compute-1.amazonaws.com:/home/ubuntu

ssh  -i $PEM ubuntu@ec2-3-87-195-146.compute-1.amazonaws.com "bash runOnDocker.sh"