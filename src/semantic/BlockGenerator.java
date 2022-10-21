package semantic;

import common.SemanticException;
import midcode.instrument.BackFill;
import midcode.BasicBlock;
import midcode.instrument.Goto;
import midcode.instrument.Return;
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

    protected final void dealBlockItem(BlockItem blockItem) {
        if(blockItem instanceof If){
            BackFill ifBackFill = new IfGenerator(currentBasicBlock, (If) blockItem).getBackFill();
            currentBasicBlock = basicBlockFactory.newBasicBlock();
            ifBackFill.fill(currentBasicBlock);
        }else if(blockItem instanceof While){
            BackFill whileBackFill = new WhileGenerator(currentBasicBlock, (While) blockItem).getBackFill();
            currentBasicBlock = basicBlockFactory.newBasicBlock();
            whileBackFill.fill(currentBasicBlock);
        }else if(blockItem instanceof Assign || blockItem instanceof GetInt || blockItem instanceof Printf ||
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
                    ExpGenerator.Result returnValue = new ExpGenerator(currentBasicBlock.getInstruments(), exp).getResult();
                    assert returnValue instanceof ExpGenerator.RValueResult;
                    basicBlockFactory.outBasicBlock(currentBasicBlock, new Return(((ExpGenerator.RValueResult) returnValue).rValue));
                }
            }else{
                basicBlockFactory.outBasicBlock(currentBasicBlock, new Return());
            }
        }else if(blockItem instanceof Block){
            dealBlock((Block) blockItem);
        }else if(blockItem instanceof Break){
            dealBreak((Break) blockItem);
        }else if(blockItem instanceof Continue){
            dealContinue((Continue) blockItem);
        }else{
            throw new SemanticException();
        }
    }

    protected abstract BackFill blockGenerator(BasicBlock basicBlock, Block block, boolean isReturn);


    protected final void dealBlock(Block block) {
        BackFill frontBlockBackFill = basicBlockFactory.outBasicBlock(currentBasicBlock, new Goto());
        currentBasicBlock = basicBlockFactory.newBasicBlock();
        frontBlockBackFill.fill(currentBasicBlock);
        symbolTable.newBlock();
        BackFill subBackFill = blockGenerator(currentBasicBlock, block, isReturn);
        symbolTable.outBlock();
        currentBasicBlock = basicBlockFactory.newBasicBlock();
        subBackFill.fill(currentBasicBlock);
    }

    protected abstract void dealBreak(Break breakItem);
    protected abstract void dealContinue(Continue continueItem);


}
