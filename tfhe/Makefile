CXX = g++
CPPFLAGS = -g -Wall -Wextra -O3
LIBS = -ltfhe-fftw
BIN = bin
RM = rm -f

all: client

client: keygen encrypt decrypt 

keygen: $(BIN)/keygen.o 
	$(CXX) $(CPPFLAGS) $(BIN)/keygen.o -o $(BIN)/keygen.out $(LIBS)

encrypt: $(BIN)/encrypt.o $(BIN)/helper.o
	$(CXX) $(CPPFLAGS) $(BIN)/encrypt.o $(BIN)/helper.o -o $(BIN)/encrypt.out $(LIBS)

decrypt: $(BIN)/decrypt.o $(BIN)/helper.o
	$(CXX) $(CPPFLAGS) $(BIN)/decrypt.o $(BIN)/helper.o -o $(BIN)/decrypt.out $(LIBS)


$(BIN)/keygen.o: client/keygen.cpp 
	$(CXX) -c client/keygen.cpp -o $(BIN)/keygen.o

$(BIN)/encrypt.o: client/encrypt.cpp 
	$(CXX) -c client/encrypt.cpp -o $(BIN)/encrypt.o

$(BIN)/decrypt.o: client/decrypt.cpp 
	$(CXX) -c client/decrypt.cpp -o $(BIN)/decrypt.o

$(BIN)/helper.o: helper.cpp helper.hpp
	$(CXX) -c helper.cpp -o $(BIN)/helper.o

clean:
	$(RM) $(BIN)/*.o $(BIN)/*.out $(BIN)/*.key
