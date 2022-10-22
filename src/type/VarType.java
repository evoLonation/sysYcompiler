package type;


public abstract class VarType{
    public int getSize(){
        return 1;
    }

    public boolean match(VarType type){
        if(this instanceof IntType && type instanceof IntType){
            return true;
        }else if(this instanceof PointerType  && type instanceof PointerType){
            if(((PointerType) this).getSecondLen().isPresent() && ((PointerType) type).getSecondLen().isPresent()){
                return ((PointerType) this).getSecondLen().get().equals(((PointerType) type).getSecondLen().get());
            }else return !((PointerType) this).getSecondLen().isPresent() && !((PointerType) type).getSecondLen().isPresent();
        }else{
            return false;
        }
    }

    protected abstract String getTypeName();
    @Override
    public String toString() {
        return getTypeName();
    }
}
