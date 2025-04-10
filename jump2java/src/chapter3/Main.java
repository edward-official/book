package chapter3;

import java.util.*;

public class Main {
    static void example1() {
        int integer1 = 1_000; //range: 2e9
        long integer2 = (long)Math.pow(10, 18); //range: 9e18, don't forget to put indicator 'L'

        System.out.println(integer1);
        System.out.println(integer2);
    }
    static void example2() {
        float realNumber1 = 0.1F; //precision: 6 decimal places, range: e38
        double realNumber2 = 2.7e-2; //precision: 15 decimal places, range:e308

        /*
        [question] why the maximum value of double is bigger than long when they're using the same number of bytes?

        reference(floating point notation in binary form):
        https://ee.usc.edu/~redekopp/cs356/slides/CS356Unit3_FP.pdf

        reference(two's complement)
        https://www.geeksforgeeks.org/twos-complement/
        */

        System.out.println(realNumber1);
        System.out.println(realNumber2);
    }
    static void example3() {
        int octal = 051;
        int hexadecimal = 0x51;

        System.out.println(octal);
        System.out.println(hexadecimal);
    }
    static void example4() {
        int test = 0;
        System.out.println(test++);
        System.out.println(test);
    }
    static void example5() {
        int score=60, average=50;
        boolean isAboveAverage = score > average;
        System.out.println(isAboveAverage);
    }
    static void example6() {
        char character1 = 'a';
        char character2 = 97; //ASCII
        char character3 = '\u0061'; //unicode
        System.out.println(character1);
        System.out.println(character2);
        System.out.println(character3);
    }
    static void example7() {
        //string is not primitive data type but still can use literal notation (special)
        String string1 = "literal";
        String string2 = "literal";
        String string3 = new String("new operator");
        String string4 = new String("new operator");

        //[think about] difference?: (answer by gpt) literal notation leads to string pool
        System.out.println(string1==string2);
        System.out.println(string3==string4);
    }
    /*
    wrapper class
    - wrapper class enables primitive data type variables to be treated as object so that they can be utilized in a way of OOP
    - ArrayList, HashMap, HashSet, ...: necessary to use their corresponding Wrapper classes instead of primitive data types
    - essential for supporting synchronization in multi-threaded environments
    */
    static void example8() {
        String line1 = "my name is lee", line2 = new String("my name is lee");
        System.out.println(line1.equals(line2));
        System.out.println(line1==line2);
        System.out.println(line1.indexOf("is"));
        System.out.println(line1.contains("edward"));
        System.out.println(line1.charAt(0));
        System.out.println(line1.replaceAll("lee", "saka"));
        System.out.println(line1.substring(0,line1.length()));
        System.out.println(line1.toUpperCase());
        System.out.println(line1.toLowerCase());
        for(String item: line1.split(" ")) System.out.println(item);
    }
    static void example9() {
        double balance = 1_000_000_000.0;
        String currency = "euro";
        System.out.println(String.format("I want %-20.2f %s", balance, currency));
        char grade = 'A';
        System.out.println(String.format("My grade is %c", grade));
        int percent = 16;
        System.out.println(String.format("percentage: %d%%(decimal)==%o%%(octal)==%x%%(hexadeciaml)", percent, percent, percent));
        System.out.println("percentage: 45%");
        System.out.println(String.format("%-10s%-10s%-10s%-10s", "java", "servlet", "spring", "jpa"));
        System.out.printf("%-10s%-10s%-10s%-10s", "java", "servlet", "spring", "jpa");
        //[think about] why println does not supply auto formatting?: gpt said historical design choices in java
    }
    static void example10() {
        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append("my name is lee\n");
        stringBuffer.append("I'm studying java\n");
        System.out.println(stringBuffer.toString());
        //[think about] what's difference with string class?: useless copies

        /*
        StringBuffer(thread-safe in a multi-threaded environment)
        StringBuilder(better performance compared to StringBuffer)
         */

        stringBuffer.insert(0, "hello, ");
        System.out.println(stringBuffer.substring(0, stringBuffer.length()));
    }
    static void example11() {
        String[] books = {"java", "servlet", "spring", "jpa"};
        for(int i=0; i<books.length; i++) System.out.println(books[i]);

        int[] numbers = new int[5];
        numbers[0]=2;
        numbers[1]=4;
        numbers[3]=6;
        numbers[4]=7;
        for(int item: numbers) System.out.println(item);
    }
    static void example12() {
        //list: arraylist, vector, linkedlist, ...
        //array(fixed size) vs list(flexible size)
        //generics: create classes, interfaces, and methods with type parameters
        //without generics, elements in list are considered as "object" type (top-level type) which leads to type casting everytime we access the element
        ArrayList<String> playerNames = new ArrayList<>();
        playerNames.add(0, "saliba");
        playerNames.add(1, "saka");
        playerNames.add(2, "white");

        for(int i=0; i<playerNames.size(); i++) System.out.println(playerNames.get(i));
        System.out.println(playerNames.contains("saka"));
        System.out.println(playerNames.remove("arteta"));
        System.out.println(playerNames.remove(0));

        Integer[] scores = {4,7,10,6};
        ArrayList<Integer> scoresList1 = new ArrayList<>(Arrays.asList(scores));
        ArrayList<Integer> scoresList2 = new ArrayList<>(Arrays.asList(4,7,10,6));
        //[think about] Integer item vs int item
        for(Integer item: scoresList1) System.out.println(item);
        for(int item: scoresList2) System.out.println(item);
    }
    static void example13() {
        String[] fruits = {"blueberry", "strawberry", "apple", "mango"};
        ArrayList<String> fruitsAsList = new ArrayList<>(Arrays.asList(fruits));
        for(String item: fruits) System.out.println(item);
        for(String item: fruitsAsList) System.out.println(item);
        String fruitsAsText1 = String.join(", ", fruitsAsList);
        String fruitsAsText2 = String.join(", ", fruits);
        System.out.println(fruitsAsText1);
        System.out.println(fruitsAsText2);
    }
    static void example14() {
        String[] players = {"saka", "odegaard", "white", "timber"};
        ArrayList<String> playersAsList = new ArrayList<>(Arrays.asList(players));
        playersAsList.sort(Comparator.naturalOrder());
        for(String item: playersAsList) System.out.println(item);
    }
    static void example15() {
        //map: hashmap, linkedhashmap, treemap, ...
        HashMap<Integer, String> players = new HashMap<>();
        players.put(22, "raya");
        players.put(7, "saka");
        players.put(5, "partey");
        players.put(4, "white");
        System.out.println(players.get(5));
        System.out.println(players.containsKey(2));
        System.out.println(players.remove(22));
        System.out.println(players.size());
        System.out.println(players.keySet()); //returned as set(can be converted into the list type)

        /*
        HashMap: no order guaranteed, hash table
        LinkedHashMap: order of insertion, hash table + linked list
        TreeMap: natural(ascending) order of key, slow, tree
         */
    }
    static void example16() {
        //set: hashset, treeset, linkedhashset, ...
        HashSet<String> names = new HashSet<>(Arrays.asList("raya", "white", "partey", "saka"));
        System.out.println(names);

        HashSet<Integer> members1 = new HashSet<>(Arrays.asList(1,4,6,8,9));
        HashSet<Integer> members2 = new HashSet<>(Arrays.asList(1,2,5,6,8));
        System.out.println(members1.removeAll(members2)); //returns boolean
        System.out.println(members1); //changes object
        System.out.println(members1.addAll(members2));
        System.out.println(members1);
        System.out.println(members1.retainAll(members2));
        System.out.println(members1);

        /*
        linkedhashset: insertion order
        treeset: ascending order
         */
    }
    enum Airlines {LOT,FINNAIR,WIZZAIR,AIRFRANCE};
    static void example17() {
        Integer[] nTicket = new Integer[Airlines.values().length];
        for(int i=0; i<Airlines.values().length; i++) {
            nTicket[i] = i;
            System.out.println("number of " + Airlines.values()[i] + " tickets: " + nTicket[i]);
        }
    }
    static void example18() {
        int age = 25;
        String ageAsString1 = String.valueOf(age);
        String ageAsString2 = Integer.toString(age);
        System.out.println(ageAsString1);
        System.out.println(ageAsString2);

        if(age==Integer.parseInt(ageAsString1)) System.out.println("type casting succeed");
        else System.out.println("type casting failed");
        if(age==Integer.parseInt(ageAsString2)) System.out.println("type casting succeed");
        else System.out.println("type casting failed");

        double rating = 4.9;
        int ratingAsInt = (int)rating;
        System.out.println(ratingAsInt);
        rating = ratingAsInt;
        System.out.println(rating);

        String averageAsString = "23.7";
        int averageAsInt = (int)Double.parseDouble(averageAsString);
        System.out.println(averageAsInt);
    }
    static void example19() {
        //final: prohibits reallocation, but I think modification inside the object is possible
        final ArrayList<String> names = new ArrayList<>(Arrays.asList("saka", "partey", "rice", "white"));
        System.out.println(names);
        names.add("magalhaes");
        System.out.println(names);

        //List.of(): generates immutable list
    }


    public static void main(String[] args) {
        example19();
    }
}
