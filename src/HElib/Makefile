HELIB_INSTALL = /opt/helib_install/helib_pack
CXX = g++
CPPFLAGS = -I $(HELIB_INSTALL)/include/ -std=c++17 -O3
LIBS = -L $(HELIB_INSTALL)/lib/ -lntl -lhelib -lgmp -lpthread
BIN = ./bin
RM = rm -f
MKDIR_P = mkdir -p

all: directories test

test: $(BIN)/test.o $(BIN)/functional_units.o
	$(CXX) $(CPPFLAGS) $(BIN)/test.o $(BIN)/functional_units.o -o $(BIN)/test.out $(LIBS)

$(BIN)/test.o: compiled/test.cpp
	$(CXX) $(CPPFLAGS) -c compiled/test.cpp -o $(BIN)/test.o

$(BIN)/functional_units.o: functional_units/functional_units.cpp
	$(CXX) $(CPPFLAGS) -c functional_units/functional_units.cpp -o $(BIN)/functional_units.o

directories: ${BIN}

${BIN}:
	${MKDIR_P} ${BIN}

clean:
	$(RM) $(BIN)/*.o $(BIN)/*.out $(BIN)/*.key $(BIN)/*.log
