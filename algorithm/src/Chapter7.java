import java.io.*;

public class Chapter7 {
    static BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
    static BufferedWriter out = new BufferedWriter(new OutputStreamWriter(System.out));

    static void p1() throws IOException {
        System.out.printf("enter text: ");
        String text = in.readLine();
        System.out.printf("enter pattern to search in the text: ");
        String pattern = in.readLine();

        int startingIndex = -1;
        for(int index=0; index<text.length()-pattern.length()+1; index++) {
            boolean isFound = true;
            for(int offset=0; offset<pattern.length(); offset++) {
                if(text.charAt(index+offset)!=pattern.charAt(offset)) isFound = false;
            }
            if(isFound) startingIndex = index;
        }

        if(startingIndex==-1) out.write("pattern not found in the text");
        else out.write("pattern found in index number " + startingIndex);
        out.newLine();
        out.flush();
    }
}
