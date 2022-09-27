import java.util.Iterator;
import java.util.LinkedList;
import java.util.NoSuchElementException;

public class MyIterator <T>{
    private Iterator<T> mIt;
    private T now;
    public MyIterator(Iterator<T> iterator) {
        mIt = iterator;
    }
    public MyIterator(Iterable<T> iterable) {
        mIt = iterable.iterator();
    }
    T now(){
        return now;
    }

    private LinkedList<T> preList = new LinkedList<>();
    void next(){
        if(!preList.isEmpty()){
            now = preList.removeFirst();
        }else{
            try{
                now = mIt.next();
            }catch (NoSuchElementException e){
                now = null;
            }
        }
    }
    T pre(int i){
        while(preList.size() < i){
            try{
                preList.addLast(mIt.next());
            }catch (NoSuchElementException e){
                return null;
            }
        }
        return preList.get(i - 1);
    }
}
