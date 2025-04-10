package chapter5;


public class Main {
    static void example1() {
        Sample1 sample1 = new Sample1();
        int n = 10;
        sample1.test(n);
        System.out.println("original data: " + n);
    }
    static void example2() {
        Sample2Counter counter = new Sample2Counter();
        Sample2Updater1 updater1 = new Sample2Updater1();
        updater1.update(counter.count);
        System.out.println(counter.count);

        Sample2Updater2 updater2 = new Sample2Updater2();
        updater2.update(counter);
        System.out.println(counter.count);
    }
    static void example3() {
        Sample3Dog dog = new Sample3Dog("pixel");
        Sample3Cat cat = new Sample3Cat("nala");
        dog.setName("pixeldowski");
        cat.setName("nalicia");
        System.out.println("name: " + dog.name);
        System.out.println("name: " + cat.name);
        dog.sound();dog.silent();
        cat.sound();cat.silent();
        dog.sound("my name is pixel");
        cat.sound("my name is nala");
        Sample3SoundMaking.soundTest("sound test");
    }


    public static void main(String[] args) {
        example3();
    }
}


class Sample1 {
    void test(int n) {
        n *= 2;
        System.out.println("inside of method: " + n);
    }
}
class Sample2Counter {
    int count = 0;
}
class Sample2Updater1 {
    void update(int count) {
        count++;
    }
}
class Sample2Updater2 {
    void update(Sample2Counter counter) {
        counter.count++;
    }
}
interface Sample3SoundMaking {
    void sound();
    String silence = "..."; //automatically designated as public static final
    default void silent() {
        System.out.println(silence);
    }
    default void sound(String sound) {
        System.out.println(sound);
    }
    static void soundTest(String sound) {
        System.out.println(sound);
    }
}
class Sample3Animal implements Sample3SoundMaking {
    String name;
    Sample3Animal(String name) {
        this.setName(name);
    }
    void setName(String name) {
        this.name = name;
    }
    /*
    all members in an interface are implicitly public,
    therefore we should designate the 'public' access modifier
     */
    public void sound() {
        System.out.println("making sound");
    }
}
class Sample3Dog extends Sample3Animal {
    Sample3Dog(String name) {
        super(name);
    }
    @Override
    public void sound() {
        System.out.println("woof");
    }
}
class Sample3Cat extends Sample3Animal {
    Sample3Cat(String name) {
        super(name);
    }
    @Override
    public void sound() {
        super.sound("meow");
    }
}
