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

    public BlockGenerator(Block block) {
        this.block = block;
    }

    @Override
    protected abstract void generate();

    /**
     * @return 是否该结束了
     */
    protected final boolean dealBlockItem(BlockItem blockItem) {
        if(blockItem instanceof If){
            BackFill ifBackFill = new IfGenerator((If) blockItem).getBackFill();
            BasicBlock afterBasicBlock = basicBlockFactory.newBasicBlock();
            ifBackFill.fill(afterBasicBlock);
        }else if(blockItem instanceof While){
            BackFill whileBackFill = new WhileGenerator((While) blockItem).getBackFill();
            BasicBlock afterBasicBlock = basicBlockFactory.newBasicBlock();
            whileBackFill.fill(afterBasicBlock);
        }else if(blockItem instanceof Assign || blockItem instanceof GetIntNode || blockItem instanceof PrintfNode ||
                blockItem instanceof Exp || blockItem instanceof Decl){
            new SingleItemGenerator(blockItem);
        }else if(blockItem instanceof ReturnNode) {
            ReturnNode returnNode = (ReturnNode)blockItem;
            if(((ReturnNode) blockItem).getExp().isPresent()){
                if(!symbolTable.nowIsReturn()){
                    errorRecorder.voidFuncReturnValue(returnNode.line());
                    basicBlockFactory.outBasicBlock(new Return());
                }else{
                    Exp exp = ((ReturnNode) blockItem).getExp().get();
                    RValue returnValue = new ExpGenerator(exp).getRValueResult();
                    basicBlockFactory.outBasicBlock(new Return( returnValue));
                }
            }else{
                basicBlockFactory.outBasicBlock(new Return());
            }
            return true;
        }else if(blockItem instanceof Block){
            dealBlock((Block) blockItem);
        }else if(blockItem instanceof Break){
            return whileStmtDealer.newBreak(((Break) blockItem).line());
        }else if(blockItem instanceof Continue){
            return whileStmtDealer.newContinue(((Continue) blockItem).line());
        }else{
            throw new SemanticException();
        }
        return false;
    }

    protected final void dealBlock(Block block){
        BackFill frontBlockBackFill = basicBlockFactory.outBasicBlock(new Goto());
        BasicBlock newBasicBlock = basicBlockFactory.newBasicBlock();
        frontBlockBackFill.fill(newBasicBlock);
        symbolTable.newBlock();
        BackFill subBackFill = new NormalBlockGenerator(block).getBackFill();
        symbolTable.outBlock();
        BasicBlock backBasicBlock = basicBlockFactory.newBasicBlock();
        subBackFill.fill(backBasicBlock);
    }
}
