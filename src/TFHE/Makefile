FTTW_FILE = /usr/local/bin/fftw-wisdom
CXX = g++
CPPFLAGS = -g -Wall -Wextra -O3
ifeq ($(shell test -e $(FTTW_FILE) && echo -n yes), yes)
$(info Compiling with FFTW)
	LIBS = -ltfhe-fftw
else
$(info Compiling with spqlios)
	LIBS = -ltfhe-spqlios-fma
endif
BIN = ./bin
RM = rm -f
MKDIR_P = mkdir -p

all: directories client test

client: keygen encrypt decrypt

keygen: $(BIN)/keygen.o
	$(CXX) $(CPPFLAGS) $(BIN)/keygen.o -o $(BIN)/keygen.out $(LIBS)

encrypt: $(BIN)/encrypt.o
	$(CXX) $(CPPFLAGS) $(BIN)/encrypt.o -o $(BIN)/encrypt.out $(LIBS)

decrypt: $(BIN)/decrypt.o
	$(CXX) $(CPPFLAGS) $(BIN)/decrypt.o -o $(BIN)/decrypt.out $(LIBS)

test: $(BIN)/test.o $(BIN)/functional_units.o
	$(CXX) $(CPPFLAGS) $(BIN)/test.o $(BIN)/functional_units.o -o $(BIN)/test.out $(LIBS)

$(BIN)/keygen.o: client/keygen.cpp
	$(CXX) -c client/keygen.cpp -o $(BIN)/keygen.o

$(BIN)/encrypt.o: client/encrypt.cpp
	$(CXX) -c client/encrypt.cpp -o $(BIN)/encrypt.o

$(BIN)/decrypt.o: client/decrypt.cpp
	$(CXX) -c client/decrypt.cpp -o $(BIN)/decrypt.o

$(BIN)/test.o: compiled/test.cpp
	$(CXX) -c compiled/test.cpp -o $(BIN)/test.o

$(BIN)/functional_units.o: functional_units/functional_units.cpp functional_units/functional_units.hpp
	$(CXX) -c functional_units/functional_units.cpp -o $(BIN)/functional_units.o

directories: ${BIN}

${BIN}:
	${MKDIR_P} ${BIN}

clean:
	$(RM) $(BIN)/*.o $(BIN)/*.out $(BIN)/*.key
