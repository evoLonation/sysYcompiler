package frontend.error;

public class Error implements Comparable<Object>{
    private final int lineno;
    private final char id;
    private final String detail;

    Error(int lineno, char id, String detail) {
        this.lineno = lineno;
        this.id = id;
        this.detail = detail;
    }

    public String simple(){
        return lineno + " " + id;
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


