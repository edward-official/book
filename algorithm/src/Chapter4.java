import java.util.Scanner;

public class Chapter4 {
    static class MockedStack {
        private int[] stack;
        private int capacity, pointer;

        public class EmptyStackException extends RuntimeException {}
        public class FullStackException extends RuntimeException {}

        public MockedStack(int capacity) {
            this.capacity = capacity;
            pointer = 0;
            try {
                stack = new int[this.capacity];
            } catch (OutOfMemoryError e) {
                this.capacity = 0;
            }
        }

        public int push(int element) throws FullStackException {
            if(pointer==capacity) throw new FullStackException();
            return stack[pointer++]=element;
        }
        public int pop() throws EmptyStackException {
            if(pointer==0) throw new EmptyStackException();
            return stack[--pointer];
        }
        public int peak() throws EmptyStackException {
            if(pointer==0) throw  new EmptyStackException();
            return stack[pointer-1];
        }
        public void clear() {
            pointer = 0;
        }
        public int indexOf(int target) {
            //scanning from the top to find out which element will be popped first
            for(int i=pointer-1; i>=0; i--) if(stack[i]==target) return i;
            return -1;
        }
        public int getCapacity() {
            return capacity;
        }
        public int getSize() {
            return pointer;
        }
        public boolean isEmpty() {
            return pointer==0;
        }
        public boolean isFull() {
            return pointer==capacity;
        }
        public void checkDetails() {
            if(isEmpty()) {
                System.out.println("empty stack");
                return;
            }
            System.out.printf("stack:");
            for(int item: stack) System.out.printf(" " + Integer.toString(item));
            System.out.println();
        }
    }
    static void p2() {
        Scanner in = new Scanner(System.in);
        System.out.printf("enter the capacity of stack: ");
        int capacity = in.nextInt();
        MockedStack mockedStack = new MockedStack(capacity);
        System.out.println("[user manual of selecting operation]");
        System.out.print("\n(1)-push\n(2)-pop\n(3)-peak\n(4)-check\n(0)-exit\n\n");

        while (true) {

            System.out.printf("operation: ");
            int selectOperation = in.nextInt();
            if(selectOperation==0) break;

            int t;
            switch(selectOperation) {
                case 1:
                    System.out.print("push: ");
                    t = in.nextInt();
                    try {
                        mockedStack.push(t);
                    } catch (MockedStack.FullStackException e) {
                        System.out.println("stack is full");
                    }
                    break;
                case 2:
                    try {
                        t = mockedStack.pop();
                        System.out.println("popped: " + t);
                    } catch (MockedStack.EmptyStackException e) {
                        System.out.println("stack is empty");
                    }
                    break;
                case 3:
                    try {
                        t = mockedStack.peak();
                        System.out.println("peak: " + t);
                    } catch (MockedStack.EmptyStackException e) {
                        System.out.println("stack is empty");
                    }
                    break;
                case 4:
                    mockedStack.checkDetails();
                    break;
            }
        }
        in.close();
    }

    @SuppressWarnings("unchecked")
    static class GenericStack<T> {
        private T[] stack;
        private int capacity, population;

        GenericStack(int capacity) {
            this.capacity = capacity;
            population = 0;
            stack = (T[]) new Object[this.capacity];
        }

        void push(T item) {
            if(population==capacity) throw new RuntimeException();
            stack[population++] = item;
        }
        T pop() {
            if(population==0) throw new RuntimeException();
            return stack[--population];
        }
        void checkDetails() {
            System.out.printf("stack:");
            for(T item: stack) System.out.printf(" " + item);
            System.out.println();
        }
    }
    static void q2() {
        final int capacity = 5;
        GenericStack<String> genericStack = new GenericStack<>(capacity);
        genericStack.push("bukayo");
        genericStack.push("white");
        genericStack.checkDetails();
    }

    static class MockedQueue {
        int[] queue;
        int capacity, startingIndex, population;

        MockedQueue(int capacity) {
            this.capacity = capacity;
            queue = new int[this.capacity];
            startingIndex = 0;
            population = 0;
        }

        public class FullQueueException extends RuntimeException {}
        public class EmptyQueueException extends RuntimeException {}

        public void enqueue(int item) {
            if(population==capacity) throw new FullQueueException();
            queue[(startingIndex+population)%capacity] = item;
            population++;
        }
        public int dequeue() {
            if(population==0) throw new EmptyQueueException();
            int removed = queue[startingIndex];
            population--;
            startingIndex=(startingIndex+1)%capacity;
            return removed;
        }
        public void checkDetails() {
            if(population==0) {
                System.out.println("queue is empty");
                return;
            }
            int index = startingIndex;
            System.out.printf("queue:");
            while(true) {
                System.out.printf(" " + queue[index]);
                index = (index+1)%capacity;
                if(index == (startingIndex+population)%capacity) break;
            }
        }
    }
    public static void p4() {
        final int capacity = 4;
        MockedQueue mockedQueue = new MockedQueue(capacity);
        mockedQueue.enqueue(7);
        mockedQueue.enqueue(5);
        mockedQueue.enqueue(4);
        mockedQueue.dequeue();
        mockedQueue.enqueue(41);
        mockedQueue.dequeue();
        mockedQueue.dequeue();
        mockedQueue.dequeue();
        mockedQueue.enqueue(15);
        mockedQueue.enqueue(22);
        mockedQueue.checkDetails();
    }
}
