CXX = g++
CPPFLAGS = -I $(HELIB_INSTALL)/include/ -std=c++17 -O3
LIBS = -L $(HELIB_INSTALL)/lib/ -lntl -lhelib -lgmp -lpthread
BIN = ./bin
RM = rm -f
MKDIR_P = mkdir -p

all: directories build

build:
	go build -o bin/test.out compiled/test.go

run:
	./bin/test.out

directories: ${BIN}

${BIN}:
	${MKDIR_P} ${BIN}

clean:
	$(RM) $(BIN)/*.o $(BIN)/*.out $(BIN)/*.key $(BIN)/*.log
