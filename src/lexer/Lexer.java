package lexer;

import common.CompileException;
import common.LexerException;
import error.ErrorRecorder;

import java.util.*;

public class Lexer {
    private final SourceIterator iterator;
    private final ErrorRecorder errorRecorder;
    public Lexer(Iterator<Character> iterator, ErrorRecorder errorRecorder){
        this.iterator = new SourceIterator(iterator);
        this.errorRecorder = errorRecorder;
    }

    public boolean isIdentifierNoDigit(char c){
        return Character.isLetter(c) || c == '_';
    }

    public List<Terminal> analysis(){
        List<Terminal> wordList = new ArrayList<>();
        while(true){
            Terminal terminal = analysisOne();
            if (terminal != null){
                wordList.add(terminal);
            }else{
                return wordList;
            }
        }
    }
    // if done, return null
    public Terminal analysisOne() {
        // 先获得字符判断接下来是数字还是标识符还是其他符号
        // 一共两种特殊符号，一种是全英文，一种是符号
        iterator.skip();
        if(iterator.now() != null){
            Character c = iterator.now();
            int nowLine = iterator.line();
            if (isIdentifierNoDigit(c)) {
                StringBuilder lexeme = new StringBuilder(c.toString());
                while (iterator.now() != null) {
                    iterator.next();
                    c = iterator.now();
                    if (isIdentifierNoDigit(c) || Character.isDigit(c)) {
                        lexeme.append(c);
                    }else{
                        break;
                    }
                }
                return getByLexeme(lexeme.toString(), nowLine);
            } else if (Character.isDigit(c)) {
                StringBuilder digits = new StringBuilder(c.toString());
                while (iterator.now() != null) {
                    iterator.next();
                    c = iterator.now();
                    if (Character.isDigit(c)) {
                        digits.append(c);
                    }else{
                        break;
                    }
                }
                return new IntConst(Integer.parseInt(digits.toString()), nowLine);
            } else if(c == '"') {
                return getSTRCON();
            } else{
                return getOperator();
            }
        }
        return null;
    }

    private Terminal getByLexeme(String lex, int nowLine) {
        TerminalType type;
        switch (lex){
            case "main" : type = TerminalType.MAINTK; break;
            case "const" : type = TerminalType.CONSTTK; break;
            case "break" : type = TerminalType.BREAKTK; break;
            case "continue" : type = TerminalType.CONTINUETK; break;
            case "int" : type = TerminalType.INTTK; break;
            case "if" : type = TerminalType.IFTK; break;
            case "else" : type = TerminalType.ELSETK; break;
            case "while" : type = TerminalType.WHILETK; break;
            case "getint" : type = TerminalType.GETINTTK; break;
            case "printf" : type = TerminalType.PRINTFTK; break;
            case "return" : type = TerminalType.RETURNTK; break;
            case "void" : type = TerminalType.VOIDTK; break;
            default: return new Ident(lex, nowLine);
        }
        return new Terminal(type, lex, nowLine);
    }
    //遇到'"'正常返回，没有则直接报错
    private FormatString getSTRCON(){
        if(iterator.now() != '"'){
            throw new LexerException();
        }
        int line = iterator.line();
        iterator.next();
        List<FormatString.Char> charList = new ArrayList<>();
        while(true){
            if(isNormalChar()){
                if(iterator.now() == '\\'){
                    iterator.next();
                    if(iterator.now() == 'n'){
                        charList.add(new FormatString.NormalChar('\n'));
                        iterator.next();
                    }else{
                        errorRecorder.illegalChar(iterator.line(), '\\');
                    }
                }else{
                    charList.add(new FormatString.NormalChar(iterator.now()));
                    iterator.next();
                }
            }else if(iterator.now() == '%'){
                iterator.next();
                if(iterator.now() == 'd'){
                    charList.add(new FormatString.FormatChar());
                    iterator.next();
                }else{
                    errorRecorder.illegalChar(iterator.line(), '%');
                }
            }else if(iterator.now() == '"') {
                iterator.next();
                break;
            }else if(iterator.now() != null){
                errorRecorder.illegalChar(iterator.line(), iterator.now());
                iterator.next();
            }else{
                throw new LexerException();
            }
        }
        return new FormatString(charList, line);
    }

    private boolean isNormalChar(){
        Character c = iterator.now();
        return c == 32 || c == 33 || c >= 40 && c <= 126;
    }

    private Terminal getOperator(){
        char c = iterator.now();
        int nowLine = iterator.line();
        iterator.next();
        String value = Character.toString(c);
        TerminalType type = null;
        switch (c) {
            case '!':
                if (iterator.now() == '=') {
                    value += '=';
                    iterator.next();
                    type = TerminalType.NEQ;
                } else {
                    type = TerminalType.NOT;
                }
                break;
            case '&':
                if (iterator.now() == '&') {
                    value += '&';
                    iterator.next();
                    type = TerminalType.AND;
                } else {
                    throw new LexerException();
                }
                break;
            case '|':
                if (iterator.now() == '|') {
                    value += '|';
                    iterator.next();
                    type = TerminalType.OR;
                } else {
                    throw new LexerException();
                }
                break;
            case '<':
                if (iterator.now() == '=') {
                    value += '=';
                    iterator.next();
                    type = TerminalType.LEQ;
                } else {
                    type = TerminalType.LSS;
                }
                break;
            case '>':
                if (iterator.now() == '=') {
                    value += '=';
                    iterator.next();
                    type = TerminalType.GEQ;
                } else {
                    type = TerminalType.GRE;
                }
                break;
            case '=':
                if (iterator.now() == '=') {
                    value += '=';
                    iterator.next();
                    type = TerminalType.EQL;
                } else {
                    type = TerminalType.ASSIGN;
                }
                break;
            case '+' :
                type = TerminalType.PLUS;
                break;
            case '-':
                type = TerminalType.MINU;
                break;
            case '*':
                type = TerminalType.MULT;
                break;
            case '/':
                type = TerminalType.DIV;
                break;
            case '%':
                type = TerminalType.MOD;
                break;
            case ';':
                type = TerminalType.SEMICN;
                break;
            case ',':
                type = TerminalType.COMMA;
                break;
            case '(':
                type = TerminalType.LPARENT;
                break;
            case ')':
                type = TerminalType.RPARENT;
                break;
            case '[':
                type = TerminalType.LBRACK;
                break;
            case ']':
                type = TerminalType.RBRACK;
                break;
            case '{':
                type = TerminalType.LBRACE;
                break;
            case '}':
                type = TerminalType.RBRACE;
                break;
        }
        return new Terminal(type, value, nowLine);
    }
}

