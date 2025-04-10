package chapter3;

import java.util.ArrayList;
import java.util.Arrays;

public class Q7 {
    public static void main(String[] args) {
        ArrayList<String> words = new ArrayList<>(Arrays.asList("my", "name", "is", "lee"));
        System.out.println(String.join(" ", words));
    }
}
