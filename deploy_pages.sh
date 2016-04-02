#!/usr/bin/env bash

set -o errexit -o nounset

rm -rf gh-pages

mkdir gh-pages

cd gh-pages

PROJECT=djeng

git clone --depth=50 --branch=gh-pages https://$GH_TOKEN@github.com/chiknrice/$PROJECT.git

RELEASE_TAG=${TRAVIS_TAG:-}

if [ ! -z ${RELEASE_TAG} ]
then
    echo "Copying release artifacts for $RELEASE_TAG"
    mkdir $PROJECT/$RELEASE_TAG
    cp -R ../build/reports/spec/* $PROJECT/$RELEASE_TAG

    # specific to djeng
    cp ../build/resources/main/djeng.xsd $PROJECT/$RELEASE_TAG/core.xsd
    cp ../build/resources/main/djeng-financial.xsd $PROJECT/$RELEASE_TAG/financial.xsd
fi

rm -rf $PROJECT/latest
mkdir $PROJECT/latest
cp -R ../build/reports/spec/* $PROJECT/latest

cd $PROJECT

echo "Updating latest concordion specs"

git config user.email "chiknrice@gmail.com"
git config user.name "Travis CI"
git config push.default matching

git add --all .
if [ ! -z ${RELEASE_TAG} ]
then
    git commit -m "update pages for release $RELEASE_TAG"
else
    git commit -m "update concordion spec result"
fi

git push

