import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.Stack;

public class Main {
    /*
    chapter 5: it was hard to convert recursive to non-recursive like q6, q8, q10
    chapter 6: types of sorting (stable/unstable, internal/external)
    chapter 6: bubble, selection, insertion > high time complexity, not efficient
    chapter 6: in quick sort, consider these (what to push first, what to choose as pivot)
    chapter 6: merge sorting is stable method
    chapter 6: 🔎 why is the time complexity of shell sorting is just square?
    chapter 6: 🔎 hard to understand the time complexity in general
     */

    public static void main(String[] args) {
        try {
            Chapter7.p1();
        } catch (IOException e) {
            System.err.println(e.getMessage());
        }
    }
}
