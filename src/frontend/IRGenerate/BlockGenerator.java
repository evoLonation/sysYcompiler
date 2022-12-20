package frontend.IRGenerate;

import common.SemanticException;
import frontend.IRGenerate.util.BackFill;
import midcode.BasicBlock;
import midcode.instruction.Goto;
import midcode.instruction.Return;
import midcode.value.RValue;
import frontend.parser.nonterminal.Block;
import frontend.parser.nonterminal.BlockItem;
import frontend.parser.nonterminal.decl.Decl;
import frontend.parser.nonterminal.exp.Exp;
import frontend.parser.nonterminal.stmt.*;


public abstract class BlockGenerator extends BasicBlockGenerator{
    protected final Block block;

    BlockGenerator(Block block) {
        this.block = block;
    }

    /**
     * @return 是否该结束了
     */
    protected final boolean dealBlockItem(BlockItem blockItem) {
        if(blockItem instanceof If){
            BackFill ifBackFill = new IfGenerator((If) blockItem).generate();
            BasicBlock afterBasicBlock = basicBlockManager.newBasicBlock();
            ifBackFill.fill(afterBasicBlock);
        }else if(blockItem instanceof While){
            BackFill whileBackFill = new WhileGenerator((While) blockItem).generate();
            BasicBlock afterBasicBlock = basicBlockManager.newBasicBlock();
            whileBackFill.fill(afterBasicBlock);
        }else if(blockItem instanceof Assign || blockItem instanceof GetIntNode || blockItem instanceof PrintfNode ||
                blockItem instanceof Exp || blockItem instanceof Decl){
            new SingleItemGenerator(blockItem).generate();
        }else if(blockItem instanceof ReturnNode) {
            ReturnNode returnNode = (ReturnNode)blockItem;
            if(((ReturnNode) blockItem).getExp().isPresent()){
                if(!symbolTable.nowIsReturn()){
                    errorRecorder.voidFuncReturnValue(returnNode.line());
                    basicBlockManager.outBasicBlock(new Return());
                }else{
                    Exp exp = ((ReturnNode) blockItem).getExp().get();
                    RValue returnValue = new ExpGenerator(exp).generate().getRValueResult();
                    basicBlockManager.outBasicBlock(new Return( returnValue));
                }
            }else{
                basicBlockManager.outBasicBlock(new Return());
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
        BackFill frontBlockBackFill = basicBlockManager.outBasicBlock(new Goto());
        BasicBlock newBasicBlock = basicBlockManager.newBasicBlock();
        frontBlockBackFill.fill(newBasicBlock);
        symbolTable.newBlock();
        BackFill subBackFill = new NormalBlockGenerator(block).generate();
        symbolTable.outBlock();
        BasicBlock backBasicBlock = basicBlockManager.newBasicBlock();
        subBackFill.fill(backBasicBlock);
    }
}
