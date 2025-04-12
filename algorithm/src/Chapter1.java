import java.util.ArrayList;
import java.util.Scanner;

public class Chapter1 {
    static void p9() {
        int positiveNumber;
        Scanner in = new Scanner(System.in);
        do {
            System.out.printf("enter the positive number: ");
            positiveNumber = in.nextInt();
        } while(positiveNumber<=0);
        System.out.println("you entered " + positiveNumber);
    }
    static void p15() {
        int nTotal = 15, nWidth = 6;
        for(int i=0; i<nTotal/nWidth; i++) System.out.println("*".repeat(nWidth));
        System.out.println("*".repeat(nTotal%nWidth));
    }


    static void q4() {
        Scanner in = new Scanner(System.in);
        System.out.printf("enter 3 integers: ");
        final int nData = 3;
        ArrayList<Integer> dataList = new ArrayList<>();
        for(int i=0; i<nData; i++) dataList.add(in.nextInt());
        for(int i=0; i<nData; i++) {
            for(int j=i+1; j<nData; j++) {
                if(dataList.get(i) > dataList.get(j)) {
                    int t = dataList.get(i);
                    dataList.set(i, dataList.get(j));
                    dataList.set(j, t);
                }
            }
        }
        System.out.println(dataList);
        System.out.println("the second element: " + dataList.get(1));
        in.close();
    }
    static void q5() {
        /*
        why is this code inefficient?
        if((b>=a && c<=a) || (b<=a && c>=a)) return a;
        else if((a>b && c<b) || (a<b && c>b)) return b;
        return c;
         */
        System.out.println("useless duplication/redundancy while comparison");
    }
    static void q11() {
        final int nSize = 10, nSpace = 4;
        String myFormat = "%" + nSpace + "d";
        System.out.printf(" ".repeat(nSpace) + "|");
        for(int i=0; i<nSize; i++) System.out.printf(myFormat, i+1);
        System.out.println("\n" + "-".repeat(nSpace) + "+" + "-".repeat(nSpace*nSize));
        for(int i=0; i<nSize; i++) {
            System.out.printf(String.format(myFormat + "|", i+1));
            for(int j=0; j<nSize; j++) {
                System.out.printf(myFormat, (i+1)*(j+1));
            }
            System.out.println();
        }
    }
}
