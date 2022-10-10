package error;

public class Error implements Comparable<Object>{
    private int lineno;
    private char id;
    private String detail;

    Error(int lineno, char id, String detail) {
        this.lineno = lineno;
        this.id = id;
        this.detail = detail;
    }

    public String simple(){
        return Integer.toString(lineno) + " " + id;
    }
    public String detail(){
        return simple() + " : " + detail;
    }


    // 前提：每一行中至多只有一个错误
    @Override
    public int compareTo(Object o) {
        return lineno - ((Error)o).lineno;
    }
}


