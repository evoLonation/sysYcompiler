package semantic;

import common.SemanticException;
import midcode.instrument.BackFill;
import midcode.BasicBlock;
import midcode.instrument.Goto;
import midcode.instrument.Return;
import midcode.value.RValue;
import parser.nonterminal.Block;
import parser.nonterminal.BlockItem;
import parser.nonterminal.decl.Decl;
import parser.nonterminal.exp.Exp;
import parser.nonterminal.stmt.*;


public abstract class BlockGenerator extends BasicBlockGenerator{
    protected final Block block;

    public BlockGenerator(BasicBlock basicBlock, Block block) {
        super(basicBlock);
        this.block = block;
        this.currentBasicBlock = basicBlock;
    }

    protected BasicBlock currentBasicBlock;


    @Override
    protected abstract void generate();

    /**
     * @return 是否该结束了
     */
    protected final boolean dealBlockItem(BlockItem blockItem) {
        if(blockItem instanceof If){
            BackFill ifBackFill = new IfGenerator(currentBasicBlock, (If) blockItem).getBackFill();
            currentBasicBlock = basicBlockFactory.newBasicBlock();
            ifBackFill.fill(currentBasicBlock);
        }else if(blockItem instanceof While){
            BackFill whileBackFill = new WhileGenerator(currentBasicBlock, (While) blockItem).getBackFill();
            currentBasicBlock = basicBlockFactory.newBasicBlock();
            whileBackFill.fill(currentBasicBlock);
        }else if(blockItem instanceof Assign || blockItem instanceof GetIntNode || blockItem instanceof PrintfNode ||
                blockItem instanceof Exp || blockItem instanceof Decl){
            new SingleItemGenerator(currentBasicBlock, blockItem);
        }else if(blockItem instanceof ReturnNode) {
            ReturnNode returnNode = (ReturnNode)blockItem;
            if(((ReturnNode) blockItem).getExp().isPresent()){
                if(!isReturn){
                    errorRecorder.voidFuncReturnValue(returnNode.line());
                    basicBlockFactory.outBasicBlock(currentBasicBlock, new Return());
                }else{
                    Exp exp = ((ReturnNode) blockItem).getExp().get();
                    RValue returnValue = new ExpGenerator(currentBasicBlock.getInstruments(), exp).getRValueResult();
                    basicBlockFactory.outBasicBlock(currentBasicBlock, new Return( returnValue));
                }
            }else{
                basicBlockFactory.outBasicBlock(currentBasicBlock, new Return());
            }
            return true;
        }else if(blockItem instanceof Block){
            dealBlock((Block) blockItem);
        }else if(blockItem instanceof Break){
            return whileStmtDealer.newBreak(currentBasicBlock, ((Break) blockItem).line());
        }else if(blockItem instanceof Continue){
            return whileStmtDealer.newContinue(currentBasicBlock, ((Continue) blockItem).line());
        }else{
            throw new SemanticException();
        }
        return false;
    }

    protected final void dealBlock(Block block){
        BackFill frontBlockBackFill = basicBlockFactory.outBasicBlock(currentBasicBlock, new Goto());
        currentBasicBlock = basicBlockFactory.newBasicBlock();
        frontBlockBackFill.fill(currentBasicBlock);
        symbolTable.newBlock();
        BackFill subBackFill = new NormalBlockGenerator(currentBasicBlock, block).getBackFill();
        symbolTable.outBlock();
        currentBasicBlock = basicBlockFactory.newBasicBlock();
        subBackFill.fill(currentBasicBlock);
    }
}
