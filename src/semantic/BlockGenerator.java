package semantic;

import midcode.BasicBlock;
import parser.nonterminal.Block;
import parser.nonterminal.BlockItem;
import parser.nonterminal.stmt.While;

public class BlockGenerator extends BasicBlockGenerator{
    private final Block block;

    public BlockGenerator(BasicBlock basicBlock, Block block, boolean isInWhile) {
        super(basicBlock);
        this.block = block;
        this.currentBasicBlock = basicBlock;
    }

    private BasicBlock currentBasicBlock;

    @Override
    protected void generate() {
        for(BlockItem item : block.getBlockItems()){
            execution.exec(item);
        }
    }

    private final VoidExecution<BlockItem> execution = new VoidExecution<BlockItem>() {
        @Override
        protected void inject() {
            inject(While.class, awhile ->{
                WhileGenerator whileGenerator = new WhileGenerator(currentBasicBlock, awhile);
                
            });


        }
    };
}
