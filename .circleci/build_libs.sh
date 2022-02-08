#!/bin/bash

set -exo pipefail

echo "Build HElib v2.2.1"
if [ ! -d "HElib/build" ] ; then
    cd ./HElib
    git reset --hard f0e3e01
    mkdir -p build && cd build
    cmake -DPACKAGE_BUILD=ON -DCMAKE_INSTALL_PREFIX=/opt/helib_install ..
    make -j2
    sudo make install
    sudo ln -s /usr/local/lib/libntl.so.44 /usr/lib/libntl.so.44
    cd ../..
else
    echo "Found in cache"
fi

echo "Build Lattigo v2.4.0"
if [ ! -d "lattigo" ] ; then
    cd ./lattigo
    git reset --hard 55f9a0247e2092a53be7630d6b2ca79021700a62
    cd ..
else
    echo "Found in cache"
fi

echo "Build PALISADE v1.11.6"
if [ ! -d "palisade-release/build" ] ; then
    cd ./palisade-release
    git reset --hard 0860127401ab794591f931fa2c61426c7b56ee2d
    mkdir -p build && cd build
    cmake ..
    make -j2
    sudo make install
    cd ../..
else
    echo "Found in cache"
fi

echo "Build SEAL v1.11.6"
if [ ! -d "SEAL/build" ] ; then
    cd ./SEAL
    git reset --hard 88bbc51
    cmake -S . -B build
    cmake --build build
    sudo cmake --install build
    cd ..
else
    echo "Found in cache"
fi

echo "Build PALISADE v1.11.6"
if [ ! -d "tfhe/build" ] ; then
    cd ./tfhe
    make -j2 && sudo make install
    sudo ln -s /usr/local/lib/libtfhe-nayuki-avx.so /usr/lib/libtfhe-nayuki-avx.so
    sudo ln -s /usr/local/lib/libtfhe-nayuki-portable.so /usr/lib/libtfhe-nayuki-portable.so
    sudo ln -s /usr/local/lib/libtfhe-spqlios-avx.so /usr/lib/libtfhe-spqlios-avx.so
    sudo ln -s /usr/local/lib/libtfhe-spqlios-fma.so /usr/lib/libtfhe-spqlios-fma.so
    cd ..
else
    echo "Found in cache"
fi
