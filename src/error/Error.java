package error;

public class Error {
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
}


