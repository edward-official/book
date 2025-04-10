package chapter3;

import java.util.ArrayList;
import java.util.Arrays;

public class Q3 {
    public static void main(String[] args) {
        String pinAsString = "000000-1111111";
        ArrayList<String> pinParts = new ArrayList<>(Arrays.asList(pinAsString.split("-")));
        System.out.println(pinParts);
    }
}
