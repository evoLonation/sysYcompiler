
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;

public class Compiler {
    public static void main(String[] args) {
        lab4();
    }
    static List<Character> getCharList(String fileName){
        List<Character> charList = new ArrayList<>();
        try {
            File file = new File(fileName);
            FileInputStream is = null;
            is = new FileInputStream(file);
            while (true) {
                int c = is.read();
                if (c == -1) {
                    break;
                }
                charList.add((char) c);
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return charList;
    }
    static void printAndWrite(String filename, String str){
        System.out.println(str);
        try{
            File outFile = new File(filename);
            FileOutputStream os = new FileOutputStream(outFile);
            os.write(str.getBytes());
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    static List<Terminal> lexer(String inputFile){
        List<Character> charList = getCharList(inputFile);
        Lexer analyser = new Lexer(charList.iterator());
        return analyser.analysis();
    }
    static Node parser(List<Terminal> terminals){
        Parser parser = new Parser(terminals.iterator());
        return parser.analysis();
    }
    static void lab4(){
        String inputFile = "testfile.txt";
        String outputFile = "output.txt";
        Node result = parser(lexer(inputFile));
        SemanticChecker checker = new SemanticChecker(result);
        checker.check();
    }
    static void lab3(){
        String inputFile = "testfile.txt";
        String outputFile = "output.txt";
        List<Word> result = parser(lexer(inputFile)).postorderWalk();
        StringBuilder str = new StringBuilder("");
        for(Word word: result){
            if(word.typeOf(Nonterminal.Decl, Nonterminal.BType, Nonterminal.BlockItem)){
                continue;
            }
            str.append(word).append("\n");
        }
        printAndWrite(outputFile, str.toString());
    }
    static void lab2(){
        String inputFile = "testfile.txt";
        String outputFile = "output.txt";
        List<Terminal> result = lexer(inputFile);
        StringBuilder str = new StringBuilder("");
        for (Terminal terminal : result) {
            str.append(terminal).append("\n");
        }
        printAndWrite(outputFile, str.toString());
    }

}
