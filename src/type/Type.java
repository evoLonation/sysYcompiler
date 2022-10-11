package type;



public abstract class Type {
    protected GenericType gType ;

    public GenericType getGenericType() {
        return gType;
    }

    public boolean is(GenericType... gType){
        for(GenericType genericType : gType){
            if(this.gType == genericType){
                return true;
            }
        }
        return false;
    }
    public boolean match(Type type){
        return is(type.getGenericType());
    }

//    public int getDimension(){
//        throw new UnsupportedOperationException();
//    }
//    public int getConstValue(){
//        throw new UnsupportedOperationException();
//    }
//    public int[] getConstValue1(){
//        throw new UnsupportedOperationException();
//    }
//    public int[][] getConstValue2(){
//        throw new UnsupportedOperationException();
//    }
//    public boolean isConst(){
//        throw new UnsupportedOperationException();
//    }
//    public int getParamNumber(){
//        throw new UnsupportedOperationException();
//    }
//    public boolean isReturn(){
//        throw new UnsupportedOperationException();
//    }
//    public List<Type> getParams() {
//        throw new UnsupportedOperationException();
//    }
}
