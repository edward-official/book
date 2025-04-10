package chapter5;

public class Q6 {
    public static void main(String[] args) {
        Calculator calculator = new Calculator();
        calculator.add(7);
        System.out.println(calculator.getValue());
    }
}

class Calculator {
    int value;
    void add(int val) {
        this.value += val;
    }
    public int getValue() {
        return this.value;
    }
}
