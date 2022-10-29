package frontend.lexer;

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
    public void next() {
        if(nowIsEnter) {
            nowIsEnter = false;
            line ++;
        }
        super.next();
        if(hasNow() && now() == '\n'){
            nowIsEnter = true;
        }
    }

    /**
     * 跳过注释和空白字符，如果都不是则不动
     */
    public void skip(){
        if(!hasNow())return;
        char nowChar = now();
        if(nowChar == '/'){
            if(!hasPre(1)){
                return;
            }
            char nextChar = pre(1);
            if(nextChar == '/'){
                // find wrapping char or finish
                next();
                next();
                while (hasNow()){
                    char commentChar = now();
                    if(commentChar == '\n' || commentChar == '\r'){
                        skip();
                        return;
                    }
                    next();
                }
            }else if(nextChar == '*') {
                next();
                next();
                // must find a "*/", so catch exception
                char commentChar1 = now();
                next();
                char commentChar2 = now();
                while(commentChar1 != '*' || commentChar2 != '/'){
                    next();
                    commentChar1 = commentChar2;
                    commentChar2 = now();
                }
                next();
                skip();
            }
        }else if (Character.isSpaceChar(nowChar) || nowChar == '\r' || nowChar == '\n' || nowChar == '\t') {
            next();
            skip();
        }
    }
}
