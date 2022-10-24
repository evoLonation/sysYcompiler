package semantic;

import midcode.instrument.BackFill;
import midcode.BasicBlock;
import parser.nonterminal.Block;
import parser.nonterminal.BlockItem;
import parser.nonterminal.exp.Number;
import parser.nonterminal.stmt.Break;
import parser.nonterminal.stmt.Continue;
import parser.nonterminal.stmt.ReturnNode;

// 直属于函数的block，需要检查最后一个语句是否有return
public class FuncBlockGenerator extends BlockGenerator{

    FuncBlockGenerator(Block block) {
        super(block);
    }

    void generate() {
        boolean dropout = false;
        for(BlockItem item : block.getBlockItems()) {
            if(dealBlockItem(item)){
                dropout = true;
                break;
            }
        }
        if(symbolTable.nowIsReturn()){
            if(block.getBlockItems().size() == 0){
                errorRecorder.returnLack(block.endLine());
                return;
            }
            BlockItem lastItem = block.getBlockItems().get(block.getBlockItems().size() - 1);
            if(lastItem instanceof ReturnNode){
                if(((ReturnNode) lastItem).getExp().isPresent()){
                    return;
                }
            }
            errorRecorder.returnLack(block.endLine());
            return;
        }
        if(!dropout){
            dealBlockItem(new ReturnNode(0));
        }
    }

}
