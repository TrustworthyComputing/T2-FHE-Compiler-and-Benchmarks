HELIB_INSTALL = /home/chaz/helib_install/helib_pack
CXX = g++
CPPFLAGS = -L $(HELIB_INSTALL)/lib/ -I $(HELIB_INSTALL)/include/ -lntl -lhelib -lgmp -lpthread -std=c++17 -O3

# NEED TO ADD HELIB TO PATH (i.e. export LD_LIBRARY_PATH=/PATH/TO/HELIB/PACK/lib/)

example: helib.cpp
	$(CXX) helib.cpp $(CPPFLAGS) -o helib_example.out

clean:
	rm *.out *.log
