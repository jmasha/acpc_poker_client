#!/bin/bash
mkdir deploy/Swordfish
mkdir deploy/Swordfish/help
mkdir deploy/Swordfish/logs
mkdir deploy/Swordfish/settings
cp settings/* deploy/Swordfish/settings
cp help/* deploy/Swordfish/help
cp -R dist/* deploy/Swordfish/.
tar -czvf deploy/Swordfish.tgz deploy/Swordfish/

