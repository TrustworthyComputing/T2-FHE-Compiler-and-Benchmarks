FROM cimg/openjdk:11.0

# Install dependencies to build HE libraries
RUN apt update
RUN apt install cmake make build-essential g++ clang autoconf javacc patchelf m4 tar lzip libfftw3-dev
RUN wget -c https://go.dev/dl/go1.17.6.linux-amd64.tar.gz
RUN sudo tar -C /usr/local -xzf go1.17.6.linux-amd64.tar.gz
RUN export PATH=$PATH:/usr/local/go/bin

# Resolve all maven project dependencies
RUN mvn dependency:go-offline

# Build T2 compiler and run tests
RUN export LD_LIBRARY_PATH="$LD_LIBRARY_PATH:/usr/local/lib" && mvn package

# Clone HE libraries
RUN .circleci/clone_libs.sh

# Build HE libraries
RUN .circleci/build_libs.sh

# Install HElib v2.2.2
# RUN cd HElib
WORKDIR HElib
RUN make install
RUN ln -sf /usr/local/lib/libntl.so.44 /usr/lib/libntl.so.44

# Install PALISADE v1.11.9
WORKDIR palisade-release/build
RUN make install
RUN ln -sf /usr/local/lib/libPALISADEcore.so.1 /usr/lib/libPALISADEcore.so.1

# Install SEAL 4.1.1
WORKDIR SEAL
RUN cmake --install build

# Install TFHE 1.0.1
WORKDIR tfhe
RUN make install
RUN ln -sf /usr/local/lib/libtfhe-nayuki-avx.so /usr/lib/libtfhe-nayuki-avx.so
RUN ln -sf /usr/local/lib/libtfhe-nayuki-portable.so /usr/lib/libtfhe-nayuki-portable.so
RUN ln -sf /usr/local/lib/libtfhe-spqlios-avx.so /usr/lib/libtfhe-spqlios-avx.so
RUN ln -sf /usr/local/lib/libtfhe-spqlios-fma.so /usr/lib/libtfhe-spqlios-fma.so

# Run T2 tests
RUN mvn test

# # Run HElib tests
# RUN .circleci/test_HElib.sh

# # Run Lattigo tests
# RUN .circleci/test_Lattigo.sh

# # Run SEAL tests
# RUN .circleci/test_SEAL.sh

# # Run TFHE tests
# RUN .circleci/test_TFHE.sh

# # Run PALISADE tests
# RUN .circleci/test_PALISADE.sh
