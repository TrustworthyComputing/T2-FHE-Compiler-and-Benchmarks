#!/bin/bash

set -exo pipefail

echo "Build HElib v2.2.2"
if [ ! -d "HElib/build" ] ; then
    cd ./HElib
    git reset --hard d7be6f0
    mkdir -p build && cd build
    cmake -DPACKAGE_BUILD=ON -DCMAKE_INSTALL_PREFIX=/opt/helib_install ..
    make -j2
    sudo make install
    sudo ln -s /usr/local/lib/libntl.so.44 /usr/lib/libntl.so.44
    cd ../..
else
    echo "Found in cache"
fi

echo "Build PALISADE v1.11.9"
if [ ! -d "palisade-release/build" ] ; then
    cd ./palisade-release
    git reset --hard 3d1f9a3f
    mkdir -p build && cd build
    cmake ..
    make -j2
    sudo make install
    sudo ln -s /usr/local/lib/libPALISADEcore.so.1 /usr/lib/libPALISADEcore.so.1
    cd ../..
else
    echo "Found in cache"
fi

echo "Build SEAL v4.1.1"
if [ ! -d "SEAL/build" ] ; then
    cd ./SEAL
    git reset --hard 206648d0e4634e5c61dcf9370676630268290b59
    cmake -S . -B build
    cmake --build build
    sudo cmake --install build
    cd ..
else
    echo "Found in cache"
fi

echo "Build TFHE v1.0.1"
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

echo "Lattigo version is defined in src/Lattigo/go.mod"
