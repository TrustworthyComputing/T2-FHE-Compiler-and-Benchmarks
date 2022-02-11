#!/bin/bash

set -exo pipefail

if [ ! -d "HElib" ] ; then
    git clone git@github.com:homenc/HElib.git
fi

if [ ! -d "SEAL" ] ; then
    git clone git@github.com:microsoft/SEAL.git
fi

if [ ! -d "palisade-release" ] ; then
    git clone https://gitlab.com/palisade/palisade-release.git
fi

if [ ! -d "tfhe" ] ; then
    git clone git@github.com:tfhe/tfhe.git
fi
