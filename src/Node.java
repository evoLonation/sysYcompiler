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
    List<Word> preorderWalk() {
        List<Word> list = new ArrayList<>();
        list.add(word);
        for(Node son : sons){
            list.addAll(son.postorderWalk());
        }
        return list;
    }
    Node addSon(Node node){
        sons.add(node);
        node.father = this;
        return this;
    }
    Node addSon(Terminal word){
        return addSon(new Node(word));
    }
    public List<Node> listSons(){
        return sons;
    }
    public Node son(int i){
        return sons.get(i);
    }
    private List<Node> sons = new ArrayList<>();
    private Node father;
    private Word word;

    public Word word() {
        return word;
    }
    public Node father(){
        return father;
    }

    public Node(Word word) {
        this.word = word;
    }
    public int line(){
        if(sons.size() == 0){
            return ((Terminal)word).line();
        }else{
            return sons.get(0).line();
        }
    }
}
