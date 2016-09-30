#!/bin/sh
set -x
echo "!!!!!!!! You need to set path to mvn to update Javadoc!!!!!!!!!!"

mvn javadoc:javadoc
cd target/site/apidocs/
git init
username=`git config user.name`
git remote add javadoc https://${username}@github.com/EnigmaBridge/retry.java
git fetch --depth=1 javadoc gh-pages
git add --all
git commit -m "Javadoc update"
git merge --no-edit -s ours remotes/javadoc/gh-pages
git push javadoc master:gh-pages
rm -r -f .git
cd ../../..
