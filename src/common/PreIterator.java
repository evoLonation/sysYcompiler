package common;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.NoSuchElementException;

public class PreIterator<T>{
    private Iterator<T> mIt;
    private T now;
    public PreIterator(Iterator<T> iterator) {
        mIt = iterator;
        next();
    }
    public PreIterator(Iterable<T> iterable) {
        mIt = iterable.iterator();
    }
    public T now(){
        return now;
    }

    private LinkedList<T> preList = new LinkedList<>();
    public T next(){
        if(!preList.isEmpty()){
            now = preList.removeFirst();
        }else{
            try{
                now = mIt.next();
            }catch (NoSuchElementException e){
                now = null;
            }
        }
        return now();
    }
    public T pre(int i){
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
