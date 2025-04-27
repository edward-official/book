import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

public class Chapter6 {
    static class BubbleSort {
        static void swap(int[] array, int index1, int index2) {
            int t = array[index1];
            array[index1] = array[index2];
            array[index2] = t;
        }
        static void sort(int[] array) {
            int performance = 0;
            for(int i=array.length-1; i>0; i--) {
                for(int j=0; j<i; j++) {
                    performance++;
                    if(array[array.length-2-j] > array[array.length-1-j]) {
                        swap(array, array.length-2-j, array.length-1-j);
                    }
                }
            }
            System.out.printf("performance: " + performance + " ");
        }
        static void sort2(int[] array) {
            int n = array.length, performance = 0;
            for(int i=1; i<n; i++) {
                int countSwap = 0;
                for(int j=0; j<n-i; j++) {
                    performance++;
                    if(array[n-2-j] > array[n-1-j]) {
                        swap(array, n-2-j, n-1-j);
                        countSwap++;
                    }
                }
                if(countSwap==0) break;
            }
            System.out.printf("performance: " + performance + " ");
        }
        static void sort3(int[] array) {
            int n = array.length, sortedRange = -1, performance = 0;
            for(int endingIndex=0; endingIndex<=n-2; endingIndex++) {
                int swapCount = 0;
                if(sortedRange!=-1) endingIndex=sortedRange;
                for(int index=n-1; index>endingIndex; index--) {
                    performance++;
                    if(array[index-1] > array[index]) {
                        swap(array, index-1, index);
                        swapCount++;
                        sortedRange = index;
                    }
                }
                if(swapCount==0) break;
            }
            System.out.printf("performance: " + performance + " ");
        }
        static void sort4(int[] array) {
            /*
            if there's no swap in a loop > break
            update left/right point
            on the both sides
             */
            final int n = array.length;
            int judge = 0, leftSortedRange = -1, rightSortedRange = n, swapCount, performance = 0;
            while(true) {
                System.out.println("\njudge: " + judge + ", " + "left: " + leftSortedRange + ", right: " + rightSortedRange + " " + Arrays.toString(array));
                swapCount = 0;
                if(judge%2==0) {
                    int recentSortedLeft = leftSortedRange;
                    for(int index=rightSortedRange-1; index>leftSortedRange+1; index--) {
                        if(array[index-1] > array[index]) {
                            performance++;
                            swap(array, index-1, index);
                            System.out.println("left swap " + Arrays.toString(array));
                            recentSortedLeft = index;
                            swapCount++;
                        }
                    }
                    leftSortedRange = recentSortedLeft-1;
                }
                else {
                    int recentSortedRight = rightSortedRange;
                    for(int index=leftSortedRange+1; index<rightSortedRange-1; index++) {
                        if(array[index] > array[index+1]) {
                            performance++;
                            swap(array, index, index+1);
                            System.out.println("right swap " + Arrays.toString(array));
                            recentSortedRight = index;
                            swapCount++;
                        }
                    }
                    rightSortedRange = recentSortedRight+1;
                }

                if(swapCount==0) {
                    System.out.printf("performance: " + performance + " ");
                    break;
                }
                judge++;
            }
        }
    }
    static class SelectionSort {
        static void swap(int[] array, int index1, int index2) {
            int buffer = array[index1];
            array[index1] = array[index2];
            array[index2] = buffer;
        }
        static void sort(int[] array, int n) {
            for(int i=0; i<n-1; i++) {
                int minimumIndex = i;
                for(int j=i+1; j<n; j++) {
                    if(array[j]<array[minimumIndex]) minimumIndex = j;
                }
                swap(array, i, minimumIndex);
            }
        }
    }
    static class InsertionSort {
        static void swap(int[] array, int index1, int index2) {
            int buffer = array[index1];
            array[index1] = array[index2];
            array[index2] = buffer;
        }
        static void sort(int[] array, int n) {
            for(int insertingIndex=1; insertingIndex<n; insertingIndex++) {
                int buffer = array[insertingIndex];
                int index;
                for(index=insertingIndex-1; index>=0; index--) {
                    if(buffer<array[index]) array[index+1] = array[index];
                    else break;
                }
                array[index+1] = buffer;
            }
        }
    }
    static class ShellSort {
        static void swap(int[] array, int index1, int index2) {
            int buffer = array[index1];
            array[index1] = array[index2];
            array[index2] = buffer;
        }
        static void sort(int[] array, int capacity) {
            int increment = capacity;
            while(increment>1) {
                increment/=2;
                System.err.println("\n" + Arrays.toString(array) + " increment: " + increment);
                for(int insertingIndex=increment; insertingIndex<capacity; insertingIndex++) {
                    int buffer = array[insertingIndex];
                    System.err.println(Arrays.toString(array) + " inserting index: " + insertingIndex);
                    int index;
                    for(index=insertingIndex-increment; index>=0; index-=increment) {
                        if(array[index]>buffer) array[index+increment] = array[index];
                        else break;
                    }
                    array[index+increment] = buffer;
                }
            }
        }
        static void sort2(int[] array, int capacity) {
            int increment = 1;
            while(increment<capacity) {
                increment *= 3;
                increment++;
            }
            while(increment>1) {
                increment/=3;
                System.err.println("\n" + Arrays.toString(array) + " increment: " + increment);
                for(int insertingIndex=increment; insertingIndex<capacity; insertingIndex++) {
                    int buffer = array[insertingIndex];
                    System.err.println(Arrays.toString(array) + " inserting index: " + insertingIndex);
                    int index;
                    for(index=insertingIndex-increment; index>=0; index-=increment) {
                        if(array[index]>buffer) array[index+increment] = array[index];
                        else break;
                    }
                    array[index+increment] = buffer;
                }
            }
        }
    }
    static class QuickSort {
        static void swap(int[] array, int index1, int index2) {
            System.err.println(Arrays.toString(array));
            int buffer = array[index1];
            array[index1] = array[index2];
            array[index2] = buffer;
            System.err.println(Arrays.toString(array) + "\n");
        }
        static void arrayTerminal(int[] array, int leftMost, int rightMost) {
            System.out.printf("[");
            for(int index=leftMost; index<rightMost; index++) System.out.printf(array[index] + ", ");
            System.out.printf(array[rightMost] + "]\n");
        }
        static void sort(int[] array, int leftMost, int rightMost) {
            int leftIndex = leftMost;
            int rightIndex = rightMost;
            int pivot = array[(leftIndex+rightIndex)/2];

            while(leftIndex<rightIndex) {
                /*
                what if array equals {2,2,2,2,2,2,2}?
                 */
                while(array[leftIndex]<pivot) leftIndex++;
                while(pivot<array[rightIndex]) rightIndex--;
                if(leftIndex<=rightIndex) swap(array, leftIndex++, rightIndex--);
            }

            if(leftMost<rightIndex) sort(array, leftMost, rightIndex);
            if(leftIndex<rightMost) sort(array, leftIndex, rightMost);
        }
        static void sortNonRecursive(int[] array, int leftMost, int rightMost) {
            Stack<Integer> indexStack = new Stack<>();
            indexStack.push(leftMost);
            indexStack.push(rightMost);

            while(!indexStack.isEmpty()) {
//                System.err.println("stack: " + indexStack);
                rightMost = indexStack.pop();
                leftMost = indexStack.pop();
                int pivot = array[(rightMost+leftMost)/2];
                int rightIndex = rightMost;
                int leftIndex = leftMost;

                while(leftIndex<rightIndex) {
                    while(array[leftIndex]<pivot) leftIndex++;
                    while(pivot<array[rightIndex]) rightIndex--;
                    if(leftIndex<=rightIndex) swap(array, leftIndex++, rightIndex--);
                }

                if(leftIndex<rightMost) {
                    indexStack.push(leftIndex);
                    indexStack.push(rightMost);
                }
                if(leftMost<rightIndex) {
                    indexStack.push(leftMost);
                    indexStack.push(rightIndex);
                }
            }
        }
        static void set(int[] array, int index1, int index2, int index3) {
            if(array[index1]>array[index2]) swap(array, index1, index2);
            if(array[index1]>array[index3]) swap(array, index1, index3);
            if(array[index2]>array[index2]) swap(array, index2, index3);
        }
        static void sort2(int[] array, int leftmost, int rightmost) {
            int leftIndex = leftmost;
            int rightIndex = rightmost;
            set(array, leftmost, (leftmost+rightmost)/2, rightmost);
            int pivot = array[(leftmost+rightmost)/2];

            while(leftIndex<rightIndex) {
                while(array[leftIndex]<pivot) leftIndex++;
                while(pivot<array[rightIndex]) rightIndex--;
                if(leftIndex<=rightIndex) swap(array, leftIndex++, rightIndex--);
            }

            if(leftmost<rightIndex) sort2(array, leftmost, rightIndex);
            if(leftIndex<rightmost) sort2(array, leftIndex, rightmost);
        }
    }
    static class MergeSort {
        static int[] buffer;

        static void recursive(int[] array, int leftmost, int rightmost) {
            if(leftmost>=rightmost) return;

            int centralIndex = (leftmost+rightmost)/2;
            recursive(array, leftmost, centralIndex);
            recursive(array, centralIndex+1, rightmost);

            int group1Index = leftmost;
            int group2Index = centralIndex+1;
            int bufferIndex = leftmost;
            while(group1Index<=centralIndex && group2Index<=rightmost) {
                if(array[group1Index]<array[group2Index]) buffer[bufferIndex++] = array[group1Index++];
                else buffer[bufferIndex++] = array[group2Index++];
            }
            while(group1Index<=centralIndex) buffer[bufferIndex++] = array[group1Index++];
            while(group2Index<=rightmost) buffer[bufferIndex++] = array[group2Index++];
            for(int index=leftmost; index<=rightmost; index++) array[index] = buffer[index];

            /*
            int i, j=0, k=leftmost, p=0;
            int center = (leftmost+rightmost)/2;

            __mergeSort(array, leftmost, center);
            __mergeSort(array, center + 1, rightmost);

            for(i=leftmost; i<=center; i++) buffer[p++] = array[i];
            while(i<=rightmost && j<p) array[k++] = (buffer[j]<=array[i]) ? buffer[j++] : array[i++];
            while(j<p) array[k++]=buffer[j++];
             */
        }
        static void sort(int[] array, int capacity) {
            buffer = new int[capacity];
            recursive(array, 0, capacity-1);
            buffer = null;
        }
    }
    static class Player {
        String name;
        GregorianCalendar birthday;

        Player(String name, GregorianCalendar birthday) {
            this.name = name;
            this.birthday = birthday;
        }
        public String toString() {return name + " [" + birthday.get(Calendar.YEAR) + "/" + birthday.get(Calendar.MONTH) + "/" + birthday.get(Calendar.DAY_OF_MONTH) + "]";}
        private static class BirthdayOrderComparator implements Comparator<Player> {
            @Override
            public int compare(Player o1, Player o2) {return (o1.birthday.compareTo(o2.birthday));}
        }
        static BirthdayOrderComparator birthdayOrderComparator = new BirthdayOrderComparator();
    }
    static class HeapSort {
        static void swap(int[] array, int index1, int index2) {
            int temp = array[index1];
            array[index1] = array[index2];
            array[index2] = temp;
        }
        static void organize(int[] array, int minimumRange, int maximumRange) {
            // on the top of the heap there's non-maximum value which has to be moved down
            int target = array[minimumRange];
            int currentIndex = minimumRange;
            while(true) {
                int biggerChildIndex;

                if(currentIndex*2+1 > maximumRange) break;
                else if(currentIndex*2+1 == maximumRange) biggerChildIndex = currentIndex*2+1;
                else {
                    if(array[currentIndex*2+1]>array[currentIndex*2+2]) biggerChildIndex = currentIndex*2+1;
                    else biggerChildIndex = currentIndex*2+2;
                }

                if(target>array[biggerChildIndex]) break;
                array[currentIndex] = array[biggerChildIndex];
                currentIndex = biggerChildIndex;
            }
            array[currentIndex] = target;
        }
        static void sort(int[] array, int capacity) {
            for(int index=(capacity-1)/2; index>=0; index--) organize(array, index, capacity-1);
            for(int targetIndex=capacity-1; targetIndex>0; targetIndex--) {
                // moving the highest value to target index
                swap(array, targetIndex, 0);
                organize(array, 0, targetIndex-1);
            }
        }
    }
    static class CountSort {
        static void sort(int[] array, int capacity, int maximumRange) {
            int[] counts = new int[maximumRange+1];
            int[] temp = new int[capacity];

            for(int index=0; index<capacity; index++) counts[array[index]]++;
            for(int index=1; index<=maximumRange; index++) counts[index] += counts[index-1];
            for(int index=capacity-1; index>=0; index--) temp[--counts[array[index]]] = array[index];
            for(int index=0; index<capacity; index++) array[index] = temp[index];
        }
    }


    static void p1() {
        int[] array = {1,3,9,4,7,8,6};
        BubbleSort.sort(array);
        System.out.println(Arrays.toString(array));
    }
    static void p2() {
        int[] array = {1,3,9,4,7,8,6};
        BubbleSort.sort2(array);
        System.out.println(Arrays.toString(array));
    }
    static void p3() {
        int[] array = {1,3,9,4,7,8,6};
        BubbleSort.sort3(array);
        System.out.println(Arrays.toString(array));
    }
    static void p4() {
        int[] array = {1,7,4,9,6,3};
        SelectionSort.sort(array, array.length);
        System.out.println(Arrays.toString(array));
    }
    static void p5() {
        int[] array = {6,4,3,7,1,9,8};
        InsertionSort.sort(array, array.length);
        System.out.println(Arrays.toString(array));
    }
    static void p6() {
        int[] array = {8,1,4,2,7,6,3,5};
        ShellSort.sort(array, array.length);
        System.out.println(Arrays.toString(array));
    }
    static void p7() {
        int[] array = {8,1,4,2,7,6,3,5};
        ShellSort.sort2(array, array.length);
        System.out.println(Arrays.toString(array));
    }
    static void p9() {
        int[] array = {1,8,7,4,5,2,6,3,9};
//        int[] array = {2,2,2,2,2,2,2};
        QuickSort.sort(array, 0, array.length-1);
        System.out.println(Arrays.toString(array));
    }
    static void p10() {
        int[] array = {5,2,7,8,9,3,0,1,4};
        QuickSort.sortNonRecursive(array, 0, array.length-1);
        System.out.println(Arrays.toString(array));
    }
    static void p11() {
        int[] array = {5,2,7,8,9,3,0,1,4};
        QuickSort.sort2(array, 0, array.length-1);
        System.out.println(Arrays.toString(array));
    }
    static void p13() {
        int[] array = {5,2,7,8,9,3,0,1,4};
        MergeSort.sort(array, array.length);
        System.out.println(Arrays.toString(array));
    }
    static void p16() {
        final int capacity = 4;
        Player[] players = new Player[capacity];
        players[0] = new Player("saka", new GregorianCalendar(2001, Calendar.SEPTEMBER, 5));
        players[1] = new Player("partey", new GregorianCalendar(1995, Calendar.JUNE, 13));
        players[2] = new Player("white", new GregorianCalendar(1997, Calendar.OCTOBER, 8));
        players[3] = new Player("rice", new GregorianCalendar(1999, Calendar.JANUARY, 14));

        Arrays.sort(players, Player.birthdayOrderComparator);
        for(int index=0; index<capacity; index++) System.out.println(players[index].toString());
    }
    static void p17() {
        int[] array = {5,2,7,8,9,3,0,1,4};
        HeapSort.sort(array, array.length);
        System.out.println(Arrays.toString(array));
    }
    static void p18() {
        int[] array = {5,2,7,8,9,3,0,1,4};
        CountSort.sort(array, array.length, 10);
        System.out.println(Arrays.toString(array));
    }


    static void q5() {
        int[] array = {9,1,3,4,6,7,8};
        BubbleSort.sort4(array);
        System.out.println(Arrays.toString(array));
    }
}
