package backend;

public class MipsSegment {

    private String content = "";

    void addInstrument(String instrument){
        content += instrument + "\n";
    }

    public String print(){
        return content;
    }
}
