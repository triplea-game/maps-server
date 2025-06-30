#!/bin/bash

# Bash script that runs a deployment
# Deployment is run as a blue-green deployment so we can do a rolling update.

# update blue environnment
ansible-playbook ansible/deploy-playbook.yml ENV=BLUE
reload nginx
wait 60 seconds
stop green environment
ansible-playbook ansible/deploy-playbook.yml ENV=GREEN
reload nginx
wait 60 seconds
stop blue environment


# Wait 30 seconds



