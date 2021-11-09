#include <iostream>
#include <vector>

using namespace std;

int main(void) {
    vector<int> v;
//    v.resize(1000);
    v(100);
    v[100] = 10;

    cout << v[100] << endl;
    return 0;
}
