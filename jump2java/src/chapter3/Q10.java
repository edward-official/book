package chapter3;

import java.util.HashMap;

public class Q10 {
    enum Coffee {HOT_AMERICANO, ICED_AMERICANO, CAFFE_LATTE}
    public static void main(String[] args) {
        HashMap<Integer, Integer> priceMap = new HashMap<>();
        priceMap.put(Coffee.HOT_AMERICANO.ordinal(), 13);
        priceMap.put(Coffee.ICED_AMERICANO.ordinal(), 14);
        priceMap.put(Coffee.CAFFE_LATTE.ordinal(), 20);

        for(int i=0; i<Coffee.values().length; i++) {
            System.out.println(Coffee.values()[i] + ": " + priceMap.get(Coffee.values()[i].ordinal()) + " euro");
        }
    }
}
