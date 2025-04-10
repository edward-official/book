package chapter5;

abstract class Material {
    int weight;
    Material(int weight) {
        this.weight = weight;
    }
    int getWeight() {
        return weight;
    }
    abstract int getValuePerWeight();
}
class Gold extends Material {
    Gold(int weight) {
        super(weight);
    }
    @Override
    int getValuePerWeight() {
        return 100;
    }
}
class Rock extends Material {
    Rock(int weight) {
        super(weight);
    }
    @Override
    int getValuePerWeight() {
        return 20;
    }
}
class MineralCalculator {
    int value;
    MineralCalculator() {
        this.value = 0;
    }
    int getValue() {
        return this.value;
    }
    void add(Material target) {
        this.value += target.getValuePerWeight() * target.getWeight();
    }
}


public class Q7 {
    public static void main(String[] args) {
        MineralCalculator calculator = new MineralCalculator();
        System.out.println(calculator.getValue());
        calculator.add(new Gold(40));
        calculator.add(new Rock(100));
        System.out.println(calculator.getValue());
    }
}
