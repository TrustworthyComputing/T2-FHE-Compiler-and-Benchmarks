HELIB_INSTALL = /opt/helib_install/helib_pack
CXX = g++
CPPFLAGS = -I $(HELIB_INSTALL)/include/ -std=c++17 -O3
LIBS = -L $(HELIB_INSTALL)/lib/ -lntl -lhelib -lgmp -lpthread
BIN = ./bin
RM = rm -f
MKDIR_P = mkdir -p

all: directories example

example: $(BIN)/helib_example.o
	$(CXX) $(CPPFLAGS) $(BIN)/helib_example.o -o $(BIN)/helib_example.out $(LIBS)

test: $(BIN)/test.o
	$(CXX) $(CPPFLAGS) $(BIN)/test.o -o $(BIN)/test.out $(LIBS)

$(BIN)/helib_example.o: examples/helib.cpp
	$(CXX) $(CPPFLAGS) -c examples/helib.cpp -o $(BIN)/helib_example.o

$(BIN)/test.o: compiled/test.cpp
	$(CXX) $(CPPFLAGS) -c compiled/test.cpp -o $(BIN)/test.o

directories: ${BIN}

${BIN}:
	${MKDIR_P} ${BIN}

clean:
	$(RM) $(BIN)/*.o $(BIN)/*.out $(BIN)/*.key $(BIN)/*.log
