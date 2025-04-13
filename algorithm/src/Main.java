import java.util.ArrayList;
import java.util.Scanner;

public class Main {
    static class Note {
        String note;
        Note() {
            note = "📝 notes\n";
        }
        void write(String target) {
            note += target + "\n";
        }
        void terminal() {
            System.out.println(note);
        }
    }


    public static void main(String[] args) {
        Note note = new Note();
        note.write("chapter 5");
        note.write("...writing recursive to non-recursive: q6, q8, q10");
        note.terminal();

        Chapter5.q10();
    }
}
