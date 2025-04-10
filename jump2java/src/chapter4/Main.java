package chapter4;

import java.util.ArrayList;
import java.util.Arrays;

public class Main {
    static void example1() {
        ArrayList<String> injuredList = new ArrayList<>(Arrays.asList("saka", "jesus", "havertz", "zinchenko"));
        if(injuredList.contains("saka") || injuredList.contains("havertz")) System.out.println("we're screwed");
        else if (injuredList.contains("jesus")) System.out.println("havertz should start as striker");
        else if (injuredList.contains("zinchenko")) System.out.println("kiwior should start as left back");
    }
    enum Emergency {CASE1, CASE2, CASE3, CASE4}
    static void example2() {
        Emergency emergency = Emergency.CASE2;
        switch (emergency) {
            case CASE1:
                System.out.println("case 1");
                break;
            case CASE2:
                System.out.println("case 2");
                break;
            case CASE3:
                System.out.println("case 3");
                break;
            case CASE4:
                System.out.println("case 4");
                break;
            default:
                System.out.println("not emergency");
        }
    }
    static void example3() {
        boolean didTreble = false;
        int year=2025, yearOfTreble = 2028;
        while(true) {
            if(year<yearOfTreble) System.out.println("we failed to do the treble in " + year++);
            else if(year==yearOfTreble) {
                System.out.println("we did it in " + year++ + "!!!");
                break;
            }
        }
    }
    static void example4() {
        int[] points = {6,8,2,3,6,6,3,6,8,1};
        for(int i=0; i<points.length; i++) {
            if(points[i]<7) continue;
            System.out.println("student " + (i+1) + " passed!!");
        }
    }
    static void example5() {
        ArrayList<String> names = new ArrayList<>(Arrays.asList("saka", "partey", "havertz", "odegaard"));
        for(String item: names) System.out.println(item);
    }


    public static void main(String[] args) {
        example5();
    }
}
