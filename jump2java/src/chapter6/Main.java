package chapter6;

import java.io.*;
import java.util.Scanner;

public class Main {
    static void example1() throws IOException {
        InputStream in = System.in;
        int ascii = in.read(); //java.io.IOException, 1byte, ASCII
        System.out.println(ascii);
    }
    static void example2() throws IOException {
        InputStream in = System.in;
        byte[] asciis = new byte[5];
        in.read(asciis);
        for(byte item: asciis) System.out.println(item);
    }
    static void example3() throws IOException {
        InputStream in = System.in;
        InputStreamReader reader = new InputStreamReader(in);
        char[] letters = new char[5];
        reader.read(letters);
        System.out.println(letters);
        /*
        InputStream: as byte(ASCII)
        InputStreamReader: as character
         */
    }
    static void example4() throws IOException {
        InputStream in = System.in;
        InputStreamReader reader = new InputStreamReader(in);
        BufferedReader bufferedReader = new BufferedReader(reader);
        String line = bufferedReader.readLine();
        System.out.println(line);
    }
    static void example5() throws IOException {
        Scanner in = new Scanner(System.in);
        System.out.println(in.next());
        in.close();
    }
    static void example6() throws IOException {
        FileOutputStream fileOutputStream = new FileOutputStream("src/chapter6/example6.txt");
        for(int i=0; i<5; i++) {
            String line = "this is line number " + (i+1) + "\n";
            fileOutputStream.write(line.getBytes());
        }
        fileOutputStream.close();
    }
    static void example7() throws IOException {
        FileWriter fileWriter = new FileWriter("src/chapter6/example7.txt");
        for(int i=0; i<5; i++) {
            String line = "this is line number " + (i+1) + "\n";
            fileWriter.write(line);
        }
        fileWriter.close();
    }
    static void example8() throws IOException {
        PrintWriter printWriter = new PrintWriter("src/chapter6/example8.txt");
        for(int i=0; i<5; i++) {
            String line = "this is line number " + (i+1);
            printWriter.println(line);
        }
        printWriter.close();
    }
    static void example9() throws IOException {
        PrintWriter printWriter = new PrintWriter("src/chapter6/example9.txt");
        for(int i=0; i<5; i++) {
            String line = "this is line number " + (i+1);
            printWriter.println(line);
        }
        printWriter.close();

        PrintWriter printWriter1 = new PrintWriter(new FileWriter("src/chapter6/example9.txt", true));
        for(int i=5; i<10; i++) {
            String line = "this is line number " + (i+1);
            printWriter1.println(line);
        }
        printWriter1.close();
    }
    static void example10() throws IOException {
        FileInputStream fileInputStream = new FileInputStream("src/chapter6/example10.txt");
        byte[] bytes = new byte[1024];
        fileInputStream.read(bytes);
        System.out.println(new String(bytes));
        fileInputStream.close();
    }
    static void example11() throws IOException {
        BufferedReader bufferedReader = new BufferedReader(new FileReader("src/chapter6/example11.txt"));
        while(true) {
            String line = bufferedReader.readLine();
            if(line==null) break;
            System.out.println(line);
        }
        bufferedReader.close();
    }


    public static void main(String[] args) throws IOException {
        example11();
    }
}
