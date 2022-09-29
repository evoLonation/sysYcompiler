package common;

public abstract class Word {
    protected String type;
    public String getType(){
        return type;
    }

    public boolean typeEqual(Word word) {
        return getType().equals(word.getType());
    }
    public boolean typeOf(String type){
        return getType().equals(type);
    }
    public boolean typeOf(String... types){
        for(String type : types){
            if(typeOf(type))return true;
        }
        return false;
    }
}
