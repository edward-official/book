import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

public class Chapter2 {
    static void p4() {
        final int nPoints = 10, maxRangeOfPoint = 100;
        int[] points = new int[nPoints];
        Random random = new Random(System.currentTimeMillis());
        for(int i=0; i<nPoints; i++) {
            points[i] = random.nextInt(maxRangeOfPoint);
            System.out.println((i+1) + " student's point: " + points[i]);
        }
        int maxPoint = points[0];
        for(int i=1; i<nPoints; i++) if(maxPoint<points[i]) maxPoint = points[i];
        System.out.println("max point: " + maxPoint);
    }
    static void p5() {
        int[] justArray = {1,2,3,4,5};
        for(int i=0; i<justArray.length/2; i++) {
            int t = justArray[i];
            justArray[i] = justArray[justArray.length-1-i];
            justArray[justArray.length-1-i] = t;
        }
        System.out.println(Arrays.toString(justArray));
    }
    static void p6() {
        int targetNumber = 42, radix = 16;
        System.out.println("converting decimal number of " + targetNumber + " to radix of " + radix);
        String radixReference = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        String result = "";
        while(targetNumber>0) {
            result += Character.toString(radixReference.charAt(targetNumber%radix));
            targetNumber /= radix;
        }
        StringBuilder stringBuilder = new StringBuilder();
        System.out.println(stringBuilder.append(result).append("x0").reverse());
    }
    static void p9() {
        int range = 1000, count = 0;
        ArrayList<Integer> primeNumbers = new ArrayList<>(Arrays.asList(2,3));
        for(int item=5; item<range; item+=2) {
            boolean isPrime = true;
            for(int indexOfDivider=1; primeNumbers.get(indexOfDivider)*primeNumbers.get(indexOfDivider)<=item; indexOfDivider++) {
                count+=2;
                if(item%primeNumbers.get(indexOfDivider)==0) {
                    isPrime=false;
                    break;
                }
            }
            if(isPrime) {
                count++;
                primeNumbers.add(item);
            }
        }
//        for(int primeNumber: primeNumbers) System.out.println(primeNumber);
        System.out.println(primeNumbers);
        System.out.println("count: " + count);
    }
    static class Person {
        String name;
        int age;
        Person(String name, int age) {
            this.name = name;
            this.age = age;
        }
        void introduce() {
            System.out.println("my name is " + name + " and I'm " + age + " years old");
        }
    }
    static void p10() {
        ArrayList<Person> members = new ArrayList<>();
        members.add(new Person("edward", 25));
        members.add(new Person("benjamin", 28));
        for(Person member: members) member.introduce();
    }
}