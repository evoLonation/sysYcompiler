import java.util.Iterator;

public class SourceIterator extends MyIterator<Character>{

    public SourceIterator(Iterator<Character> iterator) {
        super(iterator);
    }

    private int line = 1;
    int line(){
        return line;
    }
    private boolean nowIsEnter = false;

    @Override
    Character next() {
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
}
