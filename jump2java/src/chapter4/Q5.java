package chapter4;

public class Q5 {
    public static void main(String[] args) {
        int[] points = {7,3,5,8,2,8,6,2,8,4,3};
        double totalPoints = 0.0;
        for(int i=0; i<points.length; i++) totalPoints += points[i];
        System.out.println(totalPoints/points.length);
    }
}
