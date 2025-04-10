package chapter7;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.BinaryOperator;

public class Main {
    static void example1() {
        Singleton singleton1 = Singleton.getInstance();
        Singleton singleton2 = Singleton.getInstance();
        System.out.println(singleton1==singleton2);
    }
    static void example2() {
        try {
            System.out.println(1/0);
        } catch (ArithmeticException e) {
            System.err.println("execption: arithmetic!!");
        } finally {
            System.out.println("test is being operated");
        }
    }
    static void example3Comment(String comment) {
        try {
            if(comment.equals("kurwa")) throw new ViolentExpression();
            System.out.println(comment);
        } catch (ViolentExpression e) {
            System.err.println("exception: violent expression!!");
        }
    }
    static void example3() {
        example3Comment("kurwa");
        example3Comment("dzien dobry");
    }
    static void example4Comment(String comment) throws ViolentExpression {
        if(comment.equals("kurwa")) throw new ViolentExpression();
        System.out.println(comment);
    }
    static void example4() {
        try {
            example4Comment("kurwa");
            example4Comment("dzien dobry"); //how to solve this problem?: transaction control
        } catch (ViolentExpression e) {
            System.err.println("exception: violent expression!!");
        }
    }
    static void example5() {
        ArrayList<ThreadExample1> threadExamples = new ArrayList<>();
        for(int i=0; i<5; i++) {
            ThreadExample1 threadExample = new ThreadExample1(i);
            threadExample.start();
            threadExamples.add(threadExample);
        }
        for(int i=0; i<threadExamples.size(); i++) {
            try {
                threadExamples.get(i).join();
            } catch (Exception e) {}
        }
        System.out.println("main thread ends");
    }
    static void example6() {
        ArrayList<Thread> threadExamples = new ArrayList<>();
        for(int i=0; i<5; i++) {
            Thread threadExample = new Thread(new ThreadExample2(i));
            threadExample.start();
            threadExamples.add(threadExample);
        }
        for(int i=0; i<threadExamples.size(); i++) {
            try {
                threadExamples.get(i).join();
            } catch (Exception e) {}
        }
        System.out.println("main thread ends");
    }
    static void example7() {
        InterfaceWithOneMethod lambda = ((String text) -> "sound of " + text);
//        InterfaceWithOneMethod lambda = (text -> System.out.println(text));
        System.out.println(lambda.sound("london"));
    }
    static void example8() {
        BiFunction<String, Integer, String> biFunction = (text, repeat) -> text.repeat(repeat);
        System.out.println(biFunction.apply("test",2));
    }
    static void example9() {
        BinaryOperator<String> binaryOperator = (text1, text2) -> text1 + text2;
        System.out.println(binaryOperator.apply("one, ", "two, "));
    }
    static void example10() {
        ArrayList<String> names = new ArrayList<>(Arrays.asList("benjamin", "bukayo", "thomas", "kai"));
        names.stream()
                .filter(w -> w.length() > 4)
                .map(String::toUpperCase)
                .forEach(System.out::println);
    }


    public static void main(String[] args) {
        example10();
    }
}


class Singleton {
    private static Singleton instance;
    private Singleton() {
        System.out.println("singleton instance created");
    }
    public static Singleton getInstance() {
        if (instance == null) instance = new Singleton();
        return instance;
    }
}
class ViolentExpression extends RuntimeException {}
class ExceptionExample {
    static public void checkAge(int age) {
        if (age < 18) throw new RuntimeException("under aged");
        System.out.println("access permitted");
    }
}
/*
[example of transaction control]
pack() throws Exception {}
issueInvoice() throws Exception {}
ship() throws Exception {}
deliverProduct() {
    try {
        pack();
        issueInvoice();
        ship();
    } catch (Exception e) {
        cancelAll();  // If any one fails, cancel everything.
    }
}
 */
class ThreadExample1 extends Thread {
    int number;
    ThreadExample1(int number) {
        this.number = number;
    }
    @Override
    public void run() {
        System.out.println("thread " + number + " starts");
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {}
        System.out.println("thread " + number + " ends");
    }
}
class ThreadExample2 implements Runnable {
    int number;
    ThreadExample2(int number) {
        this.number = number;
    }
    @Override
    public void run() {
        System.out.println(number + " thread starts");
        try {
            Thread.sleep(1000);
        } catch (Exception e) {}
        System.out.println(number + " thread ends");
    }
}
interface InterfaceWithOneMethod {
    String sound(String text);
}
