import error.Error;
import error.ErrorRecorder;
import lexer.Lexer;
import lexer.Terminal;
import midcode.Module;
import midcode.VirtualMachine;
import parser.Parser;
import parser.nonterminal.CompUnit;
import semantic.ModuleGenerator;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;

public class Compiler {

    public static void main(String[] args) {
        lab5();
    }

    static List<Character> getCharList(String fileName){
        List<Character> charList = new ArrayList<>();
        try {
            File file = new File(fileName);
            FileInputStream is = new FileInputStream(file);
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

    static class ParserResult{
        public CompUnit compUnit;
        public List<String> postOrderList;
        public ParserResult(CompUnit compUnit, List<String> strings) {
            this.compUnit = compUnit;
            this.postOrderList = strings;
        }
    }

    static ParserResult parser(List<Terminal> terminals){
        Parser parser = new Parser(terminals);
        return new ParserResult(parser.analysis(), parser.getPostOrderList());
    }

    static void lab5(){
        String srcFile = "testfile.txt";
        String inputFile = "input.txt";
        String outputFile = "pcoderesult.txt";
        String codeFile = "midcode.txt";
        ParserResult result = parser(lexer(srcFile));
        Module module = new ModuleGenerator(result.compUnit).getModule();
        String input = "";
        for(Character c : getCharList(inputFile)){
            input += c;
        }
        VirtualMachine virtualMachine = new VirtualMachine(module, input);
        virtualMachine.run();
//        printAndWrite(codeFile, module.print());
        printAndWrite(outputFile, virtualMachine.getStdout());
    }

    static void lab4(){
        String inputFile = "testfile.txt";
        String outputFile = "output.txt";
        String errorFile = "error.txt";
        ParserResult result = parser(lexer(inputFile));
        Module module = new ModuleGenerator(result.compUnit).getModule();
        StringBuilder str = new StringBuilder();
        ErrorRecorder errorRecorder = ErrorRecorder.getInstance();
        for(Error error : errorRecorder.getErrorSet()){
            str.append(error.simple()).append("\n");
//            str.append(error.detail()).append("\n");
        }
        printAndWrite(outputFile, module.print());
        printAndWrite(errorFile, str.toString());
    }

    static void lab3(){
        String inputFile = "testfile.txt";
        String outputFile = "output.txt";
        ParserResult result = parser(lexer(inputFile));
        Module module = new ModuleGenerator(result.compUnit).getModule();
        StringBuilder str = new StringBuilder();
        for(String word: result.postOrderList){
            if(word.equals("<Decl>") || word.equals("<BType>") || word.equals("<BlockItem>")){
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
        StringBuilder str = new StringBuilder();
        for (Terminal terminal : result) {
            str.append(terminal).append("\n");
        }
        printAndWrite(outputFile, str.toString());
    }

}
