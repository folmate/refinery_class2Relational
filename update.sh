#!/bin/bash

cd "$(dirname "$0")"
cd ..
rm -rf refinery
git clone git@github.com:folmate/refinery_class2Relational.git
mv refinery_class2Relational refinery
rm -rf refinery/.git
chmod +x refinery/update.sh
