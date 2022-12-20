import backend.Generator;
import common.SemanticException;
import frontend.error.Error;
import frontend.error.ErrorRecorder;
import frontend.lexer.Lexer;
import frontend.lexer.Terminal;
import frontend.optimization.ssa.SSA;
import midcode.Module;
import frontend.parser.Parser;
import frontend.parser.nonterminal.CompUnit;
import frontend.IRGenerate.ModuleGenerator;
import vm.VirtualMachine;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public class Compiler {


    public static void main(String[] args) {
        mips(false);
//        runMidcode(midcode(true), true);

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

    static void mips(boolean isGenerateMidCode){
        String srcFile = "testfile.txt";
        String inputFile = "input.txt";
        String codeFile = "midcode.txt";
        String mipsFile = "mips.txt";
        ParserResult result = parser(lexer(srcFile));
        Module module = new ModuleGenerator(result.compUnit).generate();
        if(ErrorRecorder.getInstance().getErrorSet().size() != 0){
            for(Error error : ErrorRecorder.getInstance().getErrorSet()){
                System.out.println(error.detail());
//            str.append(frontend.error.detail()).append("\n");
            }
            throw new SemanticException();
        }
        if(isGenerateMidCode){
            printAndWrite(codeFile, module.print());
        }
        printAndWrite(mipsFile, new Generator(module).generate());
    }
    static Module midcode(boolean isGenerateMidCode){
        String srcFile = "testfile.txt";
        String codeFile = "midcode.txt";
        ParserResult result = parser(lexer(srcFile));
        Module module = new ModuleGenerator(result.compUnit).generate();
        if(ErrorRecorder.getInstance().getErrorSet().size() != 0){
            for(Error error : ErrorRecorder.getInstance().getErrorSet()){
                System.out.println(error.detail());
//            str.append(frontend.error.detail()).append("\n");
            }
            throw new SemanticException();
        }
        if(isGenerateMidCode){
            printAndWrite(codeFile, module.print());
        }
        return module;
    }
    static void optimization(Module module){
        Stream.concat(Stream.of(module.getMainFunc()), module.getOtherFunctions().stream()).forEach(function -> {
            new SSA(function.getEntry(), function.getOtherBasicBlocks()).execute();
        });
    }

    static void runMidcode(Module module, boolean isStdin){
        String inputFile = "input.txt";
        String outputFile = "pcoderesult.txt";
        VirtualMachine virtualMachine;
        if(isStdin){
            virtualMachine = new VirtualMachine(module);
        }else{
            String input = "";
            for(Character c : getCharList(inputFile)){
                input += c;
            }
            virtualMachine = new VirtualMachine(module, input);
        }
        virtualMachine.run();
        printAndWrite(outputFile, virtualMachine.getStdout());
    }

    static void lab4(){
        String inputFile = "testfile.txt";
        String outputFile = "output.txt";
        String errorFile = "frontend.error.txt";
        ParserResult result = parser(lexer(inputFile));
        Module module = new ModuleGenerator(result.compUnit).generate();
        if(ErrorRecorder.getInstance().getErrorSet().size() != 0){
            throw new SemanticException();
        }
        StringBuilder str = new StringBuilder();
        ErrorRecorder errorRecorder = ErrorRecorder.getInstance();
        for(Error error : errorRecorder.getErrorSet()){
            str.append(error.simple()).append("\n");
//            str.append(frontend.error.detail()).append("\n");
        }
        printAndWrite(outputFile, module.print());
        printAndWrite(errorFile, str.toString());
    }

    static void lab3(){
        String inputFile = "testfile.txt";
        String outputFile = "output.txt";
        ParserResult result = parser(lexer(inputFile));
        Module module = new ModuleGenerator(result.compUnit).generate();
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
