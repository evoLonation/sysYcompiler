package common;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.NoSuchElementException;

public class PreIterator<T>{
    private final Iterator<T> iterator;
    private T now;
    private T previous;
    public PreIterator(Iterator<T> iterator) {
        this.iterator = iterator;
        next();
    }
    public PreIterator(Iterable<T> iterable) {
        this(iterable.iterator());
    }
    public T now() throws NoSuchElementException{
        if(now == null)throw new NoSuchElementException();
        return now;
    }
    public boolean hasNow(){
        return now != null;
    }
    public boolean hasPre(int i){
        while(preList.size() < i){
            if(!iterator.hasNext())return false;
            preList.addLast(iterator.next());
        }
        return true;
    }

    private final LinkedList<T> preList = new LinkedList<>();
    public void next(){
        previous = now;
        if(!preList.isEmpty()){
            now = preList.removeFirst();
        }else{
            try{
                now = iterator.next();
            }catch (NoSuchElementException e){
                now = null;
            }
        }
    }

    public T pre(int i) throws NoSuchElementException{
        while(preList.size() < i){
            preList.addLast(iterator.next());
        }
        return preList.get(i - 1);
    }
    public T previous(){
        return previous;
    }
}
