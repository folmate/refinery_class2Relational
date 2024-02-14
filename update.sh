#!/bin/bash

REFINERY_OLD_LOC=$(pwd)
cd "$(dirname "$0")"
cd ..
rm -rf refinery
git clone git@github.com:folmate/refinery_class2Relational.git
mv refinery_class2Relational refinery
rm -rf refinery/.git
cd REFINERY_OLD_LOC

