package chapter3;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;

public class Q9 {
    public static void main(String[] args) {
        ArrayList<Integer> numbers = new ArrayList<>(Arrays.asList(4,7,3,1,6,7,3,2,5,8));
        HashSet<Integer> numbersSet = new HashSet<>(numbers);
        System.out.println(numbersSet);
    }
}
