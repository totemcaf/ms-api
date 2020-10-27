#!/bin/bash

sudo docker build -t ms .

sudo docker stop ms || true
sudo docker rm ms || true

sudo docker run -d --name ms -p 8080:8080 ms
