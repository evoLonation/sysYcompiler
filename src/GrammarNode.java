import java.util.ArrayList;
import java.util.List;

public class GrammarNode {
    List<Word> postorderWalk(){
        List<Word> list = new ArrayList<>();
        for(GrammarNode son : sons){
            list.addAll(son.postorderWalk());
        }
        list.add(word);
        return list;
    }
    GrammarNode addSon(GrammarNode node){
        sons.add(node);
        return this;
    }
    GrammarNode addSon(Terminal word){
        sons.add(new GrammarNode(word));
        return this;
    }
    public List<GrammarNode> sons = new ArrayList<>();
    public Word word;
    public GrammarNode(Word word) {
        this.word = word;
    }
}
