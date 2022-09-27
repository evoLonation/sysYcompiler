abstract class Word {
    protected String type;
    String getType(){
        return type;
    }

    boolean typeEqual(Word word) {
        return getType().equals(word.getType());
    }
    boolean typeOf(String type){
        return getType().equals(type);
    }
    boolean oneOf(String... types){
        for(String type : types){
            if(typeOf(type))return true;
        }
        return false;
    }
}
