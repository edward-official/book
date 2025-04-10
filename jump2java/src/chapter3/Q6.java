package chapter3;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;

public class Q6 {
    public static void main(String[] args) {
        ArrayList<Integer> pins = new ArrayList<>(Arrays.asList(4,6,3,1));
        pins.sort(Comparator.reverseOrder());
        System.out.println(pins);
    }
}
