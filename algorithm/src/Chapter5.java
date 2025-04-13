import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Stack;

public class Chapter5 {
    static void debugLogger(String out) {
        System.out.println("🔎" + out);
    }
    static class Recursive {//not efficient algorithm but just for study
        static int factorial(int n) {
            if(n==0) return 1;
            else return n * factorial(n-1);
        }
        static int gcd(int big, int small) {
            if(small==0) return big;
            else return gcd(small, big%small);
        }
        static HashMap<Integer, String> p3Map = new HashMap<>();
        static void p3Improved3(int n) {
            if(p3Map.containsKey(n)) {
                if(n<=0) return;
//                System.out.println("using saved data: " + p3Map.get(n));
                for(int i=0; i<p3Map.get(n).length(); i++) System.out.println(p3Map.get(n).charAt(i));
            }
            else if(n>0) {
                p3Improved3(n-1);
                System.out.println(n);
                p3Improved3(n-2);
                if(!p3Map.containsKey(n)) {
                    p3Map.putIfAbsent(n, p3Map.get(n-1) + n + p3Map.get(n-2));
//                    System.out.println("data saved: " + p3Map.get(n));
                }
            }
            else p3Map.put(n, "");
        }
    }
    static void p1() {
        final int n = 5;
        if(n<0) {
            System.out.println("wrong input, please enter integer greater than zero");
            return;
        }
        System.out.println("factorial: " + Recursive.factorial(5));
    }
    static void p2() {
        final int big = 48, small = 18;
        if(big==0) {
            System.out.println("wrong input");
            return;
        }
        else if(Math.abs(big)<Math.abs(small)) {
            System.out.println("wrong input");
            return;
        }
        System.out.println("gcd: " + Recursive.gcd(Math.abs(big),Math.abs(small)));
    }
    static class NonRecursive {
        static void p3Improved(int n) {
            while(n>0) {
                p3Improved(n-1);
                System.out.println(n);
                n = n-2;
            }
        }
        static void p3Improved2(int n) {
            Stack<Integer> integers = new Stack<>();
            while(true) {
                if(n>0) {
                    debugLogger("remembers " + n);
                    integers.push(n);
                    n-=1;
                    debugLogger("brings " + n);
                    continue;
                }
                if(!integers.isEmpty()) {
                    n = integers.pop();
                    debugLogger("prints remembered: " + n);
                    System.out.println(n);
                    n-=2;
                    debugLogger("brings " + n);
                    continue;
                }
                break;
            }
        }
    }
    static void p4() {
        NonRecursive.p3Improved(4);
    }
    static void p5() {
        NonRecursive.p3Improved2(4);
    }
    static void p6() {
        Recursive.p3Improved3(4);
    }
    static void hanoi(int quantity, int from, int to) {
        if(quantity>0) {
            hanoi(quantity-1, from, 6-from-to);
            System.out.println("move " + quantity +  " from " + from + " to " + to);
            hanoi(quantity-1, 6-from-to, to);
        }
    }
    static void p7() {
        hanoi(4, 1, 3);
    }
    static class Queen {
        static final int boardSize = 8;
        static int[] boardInformation = new int[boardSize];
        static boolean[] boardFlagsHorizontal = new boolean[boardSize];
        static boolean[] boardFlagsIncreasingDiagonal = new boolean[boardSize*2-1];
        static boolean[] boardFlagsDecreasingDiagonal = new boolean[boardSize*2-1];

        static void showBoardDetails() {
            System.out.println("board information:");
            for(int i=0; i<boardSize; i++) System.out.print(" " + boardInformation[i]);
            System.out.println();
        }
        static boolean isAvailable(int row, int column) {
            if(boardFlagsHorizontal[row]==true) return false;
            else if(boardFlagsIncreasingDiagonal[row+column]==true) return false;
            else if(boardFlagsDecreasingDiagonal[boardSize-1-row+column]==true) return false;
            else return true;
        }
        static void set(int column) {
            for(int row=0; row<boardSize; row++) {
                if(isAvailable(row,column)) {
                    boardInformation[column] = row;
                    if(column==boardSize-1) showBoardDetails();
                    else {
                        boardFlagsHorizontal[row] = boardFlagsIncreasingDiagonal[row+column] = boardFlagsDecreasingDiagonal[boardSize-1-row+column] = true;
                        set(column+1);
                        boardFlagsHorizontal[row] = boardFlagsIncreasingDiagonal[row+column] = boardFlagsDecreasingDiagonal[boardSize-1-row+column] = false;
                    }
                }
            }
        }
    }
    static void p9() {
        Queen.set(0);
    }

    static int gcd(int big, int small) throws RuntimeException {
        int bigParameter = Math.abs(big), smallParameter = Math.abs(small), gcd = -1;
        if(big==0) throw new RuntimeException("wrong input");
        else if(Math.abs(big)<Math.abs(small)) throw new RuntimeException("wrong input");
        int remember;
        while(true) {
            if(smallParameter==0) {
                gcd = bigParameter;
                break;
            }
            else {
                remember = smallParameter;
                smallParameter = bigParameter % smallParameter;
                bigParameter = remember;
            }
        }
        return gcd;
    }
    static void q2() {
        final int big = 48, small = 18;
//        final int big = 0, small = 0;
        try {
            int gcd = gcd(big, small);
            if(gcd>0) System.out.println("gcd: " + gcd);
            else System.out.println("error");
        }
        catch (RuntimeException e) {
            System.out.println(e.getMessage());
        }
    }
    static void q3() {
        final int[] numbers = {15,3,24,6};
        int localGCD = numbers[0];
        for(int i=1; i<numbers.length; i++) {
            if(Math.abs(localGCD) >= Math.abs(numbers[i])) localGCD = gcd(localGCD, numbers[i]);
            else localGCD = gcd(numbers[i], localGCD);
        }
        System.out.println("gcd: " + localGCD);
    }
    static void q6() {
        int n = 4;
        HashMap<Integer, String> hashMap = new HashMap<>();
        hashMap.put(0, "");
        hashMap.put(1, "1");
        if(n==1) System.out.println(hashMap.get(1));
        for(int i=2; i<=n; i++) hashMap.put(i,hashMap.get(i-1)+hashMap.get(i-2)+i);
        System.out.println(hashMap.get(n));
    }
    static class HanoiNode {
        int quantity, from, to;
        boolean isLeftChildFinished, isRightChildFinished;
        HanoiNode(int quantity, int from, int to) {
            this.quantity = quantity;
            this.from = from;
            this.to = to;

            if(this.quantity==1) {
                isLeftChildFinished = true;
                isRightChildFinished = true;
            }
            else {
                isLeftChildFinished = false;
                isRightChildFinished = false;
            }
        }
    }
    static void hanoiNonRecursive(int quantity, int from, int to) {
        if(quantity<=0) {
            System.err.println("quantity must be greater than zero");
            return;
        }

        Stack<HanoiNode> nodes = new Stack<>();
        HanoiNode currentNode = new HanoiNode(quantity, from, to);
        while(true) {
            quantity = currentNode.quantity;
            from = currentNode.from;
            to = currentNode.to;

            if(currentNode.isLeftChildFinished==false) {//(X,X): push > left
                currentNode.isLeftChildFinished=true;
                nodes.push(currentNode);
                currentNode = new HanoiNode(quantity-1,from,6-from-to);
            }
            else if(currentNode.isRightChildFinished==true) {//(O,O): pop
                System.out.println(String.format("move %d from %d to %d.", quantity, from, to));
                if(nodes.isEmpty()) break;
                currentNode=nodes.pop();
                currentNode.isLeftChildFinished=true;
            }
            else {//(O,X): print > right
                System.out.println(String.format("move %d from %d to %d.", quantity, from, to));
                currentNode.isRightChildFinished=true;
                currentNode = new HanoiNode(quantity-1, 6-from-to, to);
            }
        }
    }
    static void q8() {
        hanoiNonRecursive(4,1,3);
    }
    static class QueenNonRecursive {
        /*
        starting at first column
        find the possible rows in the given column
        if found, move on to the next column
        if not found, back to the previous column

        how to check if the solution is complete
        when to conclude to stop and check different solution
        how to go back to the next possible solution
        when to escape from the loop

        what happens when we find a possible row in a column?
        - update the flag
        - update the solution
         */
        static final int sizeOfBoard = 8, sizeOfDiagonal = sizeOfBoard*2-1;
        static int[] solution = new int[sizeOfBoard];
        static boolean[] flagHorizontal = new boolean[sizeOfBoard];
        static boolean[] flagDecreasing = new boolean[sizeOfDiagonal];
        static boolean[] flagIncreasing = new boolean[sizeOfDiagonal];
        static Stack<String> solutions = new Stack<>();


        static void initialize() {
            for(int i=0; i<sizeOfBoard; i++) {
                solution[i] = -1;
                flagHorizontal[i] = false;
            }
            for(int i=0;i<sizeOfDiagonal; i++) {
                flagDecreasing[i] = false;
                flagIncreasing[i] = false;
            }
        }
        static int indexDecreasing(int currentColumn, int currentRow) {
            return currentColumn-currentRow+sizeOfBoard-1;
        }
        static int indexIncreasing(int currentColumn, int currentRow) {
            return currentColumn+currentRow;
        }
        static void updateFlag(int currentColumn, int currentRow, boolean flag) {
            flagHorizontal[currentRow] = flag;
            flagDecreasing[indexDecreasing(currentColumn, currentRow)] = flag;
            flagIncreasing[indexIncreasing(currentColumn, currentRow)] = flag;
        }
        static boolean isPossible(int currentColumn, int currentRow) {
            if(flagHorizontal[currentRow]) return false;
            else if(flagDecreasing[indexDecreasing(currentColumn, currentRow)]) return false;
            else if(flagIncreasing[indexIncreasing(currentColumn, currentRow)]) return false;
            return true;
        }
        static void debug(int currentColumn, int currentRow) {
            String text = "column: " + currentColumn + ", row: " + currentRow;
            if(isPossible(currentColumn,currentRow)) text += " [O] ";
            else text += " [X] ";
            text += Arrays.toString(solution);
            System.out.printf(text + " ");
            terminalFlag();
            if(isPossible(currentColumn,currentRow)) System.out.println();
        }
        static void terminalFlag() {
            for(boolean item: flagHorizontal) {
                if(item) System.out.printf("O");
                else System.out.printf("X");
            }
            System.out.printf(" | ");
            for(boolean item: flagDecreasing) {
                if(item) System.out.printf("O");
                else System.out.printf("X");
            }
            System.out.printf(" | ");
            for(boolean item: flagIncreasing) {
                if(item) System.out.printf("O");
                else System.out.printf("X");
            }
            System.out.println();
        }


        static void run() {
            initialize();
            int currentColumn = 0, currentRow = 0;
            while(true) {
                debug(currentColumn, currentRow);
                for(int i=currentColumn; i<sizeOfBoard; i++) solution[i]=-1;

                if(isPossible(currentColumn, currentRow)) {
                    solution[currentColumn] = currentRow;
                    updateFlag(currentColumn,currentRow,true);
                    if(currentColumn==sizeOfBoard-1) {
                        solutions.push(Arrays.toString(solution));
                        System.out.println("pushed: " + Arrays.toString(solution));
                        updateFlag(currentColumn,currentRow,false);
                        //back to the previous solution (redundant)
                        if(solution[--currentColumn]==sizeOfBoard-1) {
                            updateFlag(currentColumn,solution[currentColumn],false);
                            currentRow = ++solution[--currentColumn];
                        }
                        else currentRow = ++solution[currentColumn];
                        updateFlag(currentColumn,currentRow-1,false);
                    }
                    else {
                        currentColumn++;
                        currentRow = 0;
                    }
                }
                else {
                    if(currentRow==sizeOfBoard-1) {
                        if(currentColumn==0) break;
                        else if(currentColumn==1 && solution[0]==sizeOfBoard-1) break;
                        else if(solution[--currentColumn]==sizeOfBoard-1) {
                            //back to the previous solution (redundant)
                            updateFlag(currentColumn,solution[currentColumn],false);
                            currentRow = ++solution[--currentColumn];
                        }
                        else currentRow = ++solution[currentColumn];
                        updateFlag(currentColumn,currentRow-1,false);
                    }
                    else {
                        currentRow++;
                    }
                }
            }
        }
        static void terminal() {
            System.out.println(solutions.size());
            for(String item: solutions) System.out.println(item);
        }
    }
    static void q10() {
        QueenNonRecursive.run();
        QueenNonRecursive.terminal();
    }
}
