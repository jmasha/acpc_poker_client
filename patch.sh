#!/bin/bash

rm -rf deploy
mkdir deploy
mkdir deploy/Swordfish

cp -R dist/lib deploy/Swordfish/
cp -R dist/Swordfish.jar deploy/Swordfish/.

cd deploy/

tar -czf SwordfishPatch.tgz Swordfish/

scp SwordfishPatch.tgz 129.128.184.126:~/public_html/