#!/bin/bash
remote="simm@192.168.0.102:~/homesitter/"

scp -r target/*one-jar.jar $remote
# ssh pi@192.168.0.110 "cd homesitter; python3 -m org.homesitter.homesitter"