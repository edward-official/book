import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;

public class Chapter3 {
    static void p3() {
        //sentinel method
        ArrayList<Integer> datas = new ArrayList<>(Arrays.asList(6,4,3,2,1,2,8));
        int targetData = 2, index = 0;
        System.out.println("data information: " + datas);
        System.out.println("target: " + targetData);

        System.out.println("\nsearch operation begins...");
        datas.add(targetData);
        while(true) {
            if(datas.get(index)==targetData) break;
            else index++;
        }
        if(index==datas.size()-1) index = -1;
        datas.remove(datas.size()-1);

        if(index==-1) System.out.println("target data doesn't exist");
        else System.out.println("target dats is on index number " + index);
    }
    static void p4() {
        ArrayList<Integer> datas = new ArrayList<>(Arrays.asList(6,4,3,2,1,2,8));
        datas.sort(Comparator.naturalOrder());
        int leftRange = 0, rightRange = datas.size()-1, target = 4, index;
        boolean found = false;

        System.out.println("data information: " + datas);
        System.out.println("finding " + target + "...");
        while(true) {
            index = (leftRange + rightRange)/2;
            if(datas.get(index) == target) {
                found = true;
                break;
            }
            else if(datas.get(index)<target) leftRange=index+1;
            else if(target<datas.get(index)) rightRange=index-1;
            else if(leftRange==rightRange) break;
        }

        if(found) System.out.println("data found in index " + index);
        else System.out.println("no data found");
    }
    static class Player {
        String name;
        int value;
        Player(String name, int value) {
            this.name = name;
            this.value = value;
        }

        private static class TestOrder implements Comparator<Player> {
            @Override
            public int compare(Player o1, Player o2) {
                if(o1.value>o2.value) return 1;
                else if(o1.value==o2.value) return 0;
                else return -1;
            }
        }
        static final Comparator<Player> TEST_ORDER = new TestOrder();
    }
    static void p8() {
        Player[] players = {
                new Player("David Raya", 40),
                new Player("William Saliba", 80),
                new Player("Ben White", 50),
                new Player("Declan Rice", 110),
        };
        Arrays.sort(players, Player.TEST_ORDER);

        int index = Arrays.binarySearch(players, new Player("Ben White", 50), Player.TEST_ORDER);
        if(index<0) System.out.println("no player found");
        else System.out.println("player found in index " + index);
    }


    static int binSearchX(int[] datas, int target) {
        int leftScope = 0, rightScope = datas.length-1, scopeCenter;
        int index = -1;
        while(true) {
            scopeCenter = (leftScope + rightScope) / 2;
            if(datas[scopeCenter] == target) {
                index = scopeCenter;
                rightScope = scopeCenter-1;
            }
            else if(datas[scopeCenter] < target) leftScope = scopeCenter + 1;
            else if(target < datas[scopeCenter]) rightScope = scopeCenter - 1;

            if(leftScope>=rightScope) break;
        }
        return index;
    }
    static void q5() {
        int[] datas = {1, 3, 5, 7, 7, 7, 7, 8, 8, 9, 9};
        int target = 7;
        System.out.println("data: " + Arrays.toString(datas));
        System.out.println("finding " + target + "...\n");

        int index = binSearchX(datas, target);

        if (index != -1) System.out.println("target found in index " + index);
        else System.out.println("no data found");
    }
}