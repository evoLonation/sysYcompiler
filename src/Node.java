import java.util.ArrayList;
import java.util.List;

public class Node {
    List<Word> postorderWalk(){
        List<Word> list = new ArrayList<>();
        for(Node son : sons){
            list.addAll(son.postorderWalk());
        }
        list.add(word);
        return list;
    }
    Node addSon(Node node){
        sons.add(node);
        return this;
    }
    Node addSon(Terminal word){
        sons.add(new Node(word));
        return this;
    }
    public List<Node> sons = new ArrayList<>();
    public Word word;
    public Node(Word word) {
        this.word = word;
    }
}
