FROM cimg/openjdk:11.0

USER root

# Install dependencies to build HE libraries
RUN apt-get update
RUN apt-get install cmake make build-essential g++ clang autoconf javacc patchelf m4 tar lzip libfftw3-dev
RUN wget -c https://go.dev/dl/go1.17.6.linux-amd64.tar.gz
RUN tar -C /usr/local -xzf go1.17.6.linux-amd64.tar.gz
RUN export PATH=$PATH:/usr/local/go/bin

# Copy files to docker container
COPY . .

# Resolve all maven project dependencies
RUN mvn dependency:go-offline

# Build T2 compiler and run tests
RUN export LD_LIBRARY_PATH="$LD_LIBRARY_PATH:/usr/local/lib"
RUN mvn package

# Clone HE libraries
RUN .circleci/clone_libs.sh

# Build HE libraries
RUN .circleci/build_libs.sh

# Run T2 tests
RUN mvn test
