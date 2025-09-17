#include <iostream>
using namespace std;

int main() {
    int smallest_i = 1 << (8 * sizeof(int) - 1);
    int largest_i = ~smallest_i;

    cout << "\n🚨 smallest integer" << endl;
    cout << "original: " << smallest_i << endl;
    cout << "negated: " << (-1) * smallest_i << endl;
    cout << "subtracted by 1: " << smallest_i - 1 << endl;

    cout << "\n🚨 largest integer" << endl;
    cout << "original: " << largest_i << endl;
    cout << "negated: " << (-1) * largest_i << endl;
    cout << "added by 1: " << largest_i + 1 << endl;
}