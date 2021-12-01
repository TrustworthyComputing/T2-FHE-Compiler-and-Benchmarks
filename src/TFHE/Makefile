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

all: directories client

client: keygen encrypt decrypt

benchmarks: fibonacci factorial

keygen: $(BIN)/keygen.o
	$(CXX) $(CPPFLAGS) $(BIN)/keygen.o -o $(BIN)/keygen.out $(LIBS)

encrypt: $(BIN)/encrypt.o $(BIN)/helper.o
	$(CXX) $(CPPFLAGS) $(BIN)/encrypt.o $(BIN)/helper.o -o $(BIN)/encrypt.out $(LIBS)

decrypt: $(BIN)/decrypt.o $(BIN)/helper.o
	$(CXX) $(CPPFLAGS) $(BIN)/decrypt.o $(BIN)/helper.o -o $(BIN)/decrypt.out $(LIBS)

fibonacci: $(BIN)/fibonacci.o $(BIN)/helper.o
	$(CXX) $(CPPFLAGS) $(BIN)/fibonacci.o $(BIN)/helper.o -o $(BIN)/fibonacci.out $(LIBS)

factorial: $(BIN)/factorial.o $(BIN)/helper.o
	$(CXX) $(CPPFLAGS) $(BIN)/factorial.o $(BIN)/helper.o -o $(BIN)/factorial.out $(LIBS)

pir: $(BIN)/pir.o $(BIN)/helper.o
	$(CXX) $(CPPFLAGS) $(BIN)/pir.o $(BIN)/helper.o -o $(BIN)/pir.out $(LIBS)

simon: $(BIN)/simon.o $(BIN)/helper.o
	$(CXX) $(CPPFLAGS) $(BIN)/simon.o $(BIN)/helper.o -o $(BIN)/simon.out $(LIBS)

test: $(BIN)/test.o $(BIN)/functional_units.o
	$(CXX) $(CPPFLAGS) $(BIN)/test.o $(BIN)/functional_units.o -o $(BIN)/test.out $(LIBS)

$(BIN)/keygen.o: client/keygen.cpp
	$(CXX) -c client/keygen.cpp -o $(BIN)/keygen.o

$(BIN)/encrypt.o: client/encrypt.cpp
	$(CXX) -c client/encrypt.cpp -o $(BIN)/encrypt.o

$(BIN)/decrypt.o: client/decrypt.cpp
	$(CXX) -c client/decrypt.cpp -o $(BIN)/decrypt.o

$(BIN)/fibonacci.o: Microbenchmarks/fibonacci/fibonacci.cpp
	$(CXX) -c Microbenchmarks/fibonacci/fibonacci.cpp -o $(BIN)/fibonacci.o

$(BIN)/factorial.o: Microbenchmarks/factorial/factorial.cpp
	$(CXX) -c Microbenchmarks/factorial/factorial.cpp -o $(BIN)/factorial.o

$(BIN)/pir.o: Microbenchmarks/PIR/PIR.cpp
	$(CXX) -c Microbenchmarks/PIR/PIR.cpp -o $(BIN)/pir.o

$(BIN)/simon.o: Crypto/Simon/simon.cpp
	$(CXX) -c Crypto/Simon/simon.cpp -o $(BIN)/simon.o

$(BIN)/test.o: compiled/test.cpp
	$(CXX) -c compiled/test.cpp -o $(BIN)/test.o

$(BIN)/helper.o: helper.cpp helper.hpp
	$(CXX) -c helper.cpp -o $(BIN)/helper.o

$(BIN)/functional_units.o: functional_units/functional_units.cpp functional_units/functional_units.hpp
	$(CXX) -c functional_units/functional_units.cpp -o $(BIN)/functional_units.o

directories: ${BIN}

${BIN}:
	${MKDIR_P} ${BIN}

clean:
	$(RM) $(BIN)/*.o $(BIN)/*.out $(BIN)/*.key
