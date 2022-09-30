package lexer;

import common.PreIterator;

import java.util.Iterator;

public class SourceIterator extends PreIterator<Character> {

    public SourceIterator(Iterator<Character> iterator) {
        super(iterator);
    }

    private int line = 1;
    int line(){
        return line;
    }
    private boolean nowIsEnter = false;

    @Override
    public Character next() {
        if(nowIsEnter) {
            nowIsEnter = false;
            line ++;
        }
        Character ret = super.next();
        if(ret != null && ret == '\n'){
            nowIsEnter = true;
        }
        return ret;
    }

    // 跳过注释和空白字符，如果都不是则不动
    public void skip() {
        while (now() != null) {
            char c = now();
            if(c == '/'){
                char nextC = pre(1);
                if(nextC == '/'){
                    next();
                    next();
                    Character commentChar = now();
                    while(commentChar != '\n' && commentChar != '\r' && commentChar != null){
                        next();
                        commentChar = now();
                    }
                    next();
                    skip();
                    return;
                }else if(nextC == '*') {
                    next();
                    next();
                    char commentChar1 = now();
                    next();
                    char commentChar2 = now();
                    while(commentChar1 != '*' || commentChar2 != '/'){
                        commentChar1 = commentChar2;
                        next();
                        commentChar2 = now();
                    }
                    next();
                    skip();
                    return;
                }else{
                    return;
                }
            }else if (Character.isSpaceChar(c) || c == '\r' || c == '\n' || c == '\t') {
                next();
            }else {
                return;
            }
        }
    }
}
