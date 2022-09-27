
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

public class Compiler {
    public static void main(String[] args) {
        String inputFile = "testfile.txt";
        List<Character> charList = new ArrayList<>();
        try{
            File file = new File(inputFile);
            FileInputStream is = null;
            is = new FileInputStream(file);
            while (true) {
                int c = is.read();
                if(c == -1){
                    break;
                }
                charList.add((char) c);
            }

            File outFile = new File("output.txt");
            FileOutputStream os = new FileOutputStream(outFile);

            ListIterator<Character> iterator = charList.listIterator();
            Lexer analyser = new Lexer(iterator);
            Iterator<Terminal> terminalIterator = analyser.analysis().iterator();
//            for (Iterator<Terminal> it = terminalIterator; it.hasNext(); ) {
//                WordInterface word = it.next();
//                os.write(word.toString().getBytes());
//                os.write(new byte[]{'\n', });
//                System.out.println(word);
//            }
            GrammarParser parser = new GrammarParser(new MyIterator<>(terminalIterator));
            for(Word word: parser.analysis().postorderWalk()){
                if(word.oneOf(Nonterminal.Decl, Nonterminal.BType, Nonterminal.BlockItem)){
                    continue;
                }
                os.write(word.toString().getBytes());
                os.write(new byte[]{'\n', });
                System.out.println(word);
            }
        }catch (Exception e){
            e.printStackTrace();
        }





    }
}
