package chapter5;

import java.util.ArrayList;
import java.util.Arrays;

public class Q5 {
    public static void main(String[] args) {
        ArrayList<Integer> a = new ArrayList<>(Arrays.asList(1, 2, 3));
        ArrayList<Integer> b = a;
        System.out.println(a.size());
        System.out.println(b.size());
        a.add(4);
        System.out.println(a.size());
        System.out.println(b.size());
    }
}
