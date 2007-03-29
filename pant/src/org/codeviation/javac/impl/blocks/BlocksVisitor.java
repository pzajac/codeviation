
package org.codeviation.javac.impl.blocks;
import com.sun.source.tree.AnnotationTree;
import com.sun.source.tree.ArrayAccessTree;
import com.sun.source.tree.ArrayTypeTree;
import com.sun.source.tree.AssertTree;
import com.sun.source.tree.AssignmentTree;
import com.sun.source.tree.BinaryTree;
import com.sun.source.tree.BlockTree;
import com.sun.source.tree.BreakTree;
import com.sun.source.tree.CaseTree;
import com.sun.source.tree.CatchTree;
import com.sun.source.tree.CompoundAssignmentTree;
import com.sun.source.tree.ConditionalExpressionTree;
import com.sun.source.tree.ContinueTree;
import com.sun.source.tree.DoWhileLoopTree;
import com.sun.source.tree.EmptyStatementTree;
import com.sun.source.tree.EnhancedForLoopTree;
import com.sun.source.tree.ExpressionStatementTree;
import com.sun.source.tree.ForLoopTree;
import com.sun.source.tree.IdentifierTree;
import com.sun.source.tree.IfTree;
import com.sun.source.tree.ImportTree;
import com.sun.source.tree.InstanceOfTree;
import com.sun.source.tree.LabeledStatementTree;
import com.sun.source.tree.LiteralTree;
import com.sun.source.tree.MemberSelectTree;
import com.sun.source.tree.MethodInvocationTree;
import com.sun.source.tree.ModifiersTree;
import com.sun.source.tree.NewArrayTree;
import com.sun.source.tree.NewClassTree;
import com.sun.source.tree.ParenthesizedTree;
import com.sun.source.tree.PrimitiveTypeTree;
import com.sun.source.tree.ReturnTree;
import com.sun.source.tree.SwitchTree;
import com.sun.source.tree.SynchronizedTree;
import com.sun.source.tree.ThrowTree;
import com.sun.source.tree.TryTree;
import com.sun.source.tree.TypeCastTree;
import com.sun.source.tree.TypeParameterTree;
import com.sun.source.tree.UnaryTree;
import com.sun.source.tree.VariableTree;
import com.sun.source.tree.WhileLoopTree;
import com.sun.source.tree.WildcardTree;
import java.util.Stack;
import com.sun.source.tree.ClassTree;
import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.ErroneousTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.ParameterizedTypeTree;
import com.sun.source.tree.StatementTree;
import com.sun.source.tree.Tree;
import com.sun.source.util.TreePath;
import com.sun.source.util.TreePathScanner;
import com.sun.source.util.Trees;
import com.sun.tools.javac.api.JavacTaskImpl;
import com.sun.tools.javac.code.Symbol;
import com.sun.tools.javac.code.Symbol.ClassSymbol;
import com.sun.tools.javac.code.Type.ArrayType;
import com.sun.tools.javac.code.Types;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.util.Name;
import java.util.List;
import java.util.Set;
import java.util.Stack;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.tools.JavaFileManager;
import javax.tools.StandardLocation;
import org.netbeans.modules.java.source.usages.ClassFileUtil;

/**
 *Based on UsageVisitor from retouche.
 * @author pzajac
 */
    public  class BlocksVisitor extends TreePathScanner<Void,Blocks> {
        static final Logger logger = Logger.getLogger(BlocksVisitor.class.getName());
        private Trees trees;
        
        private JavaFileManager manager;
        private final JavacTaskImpl jt;
        private final Name errorName;
        private final CompilationUnitTree cu;        
        private final Types types;
        private final javax.tools.JavaFileObject sibling;
        private final String sourceName;
        private Element enclosingElement = null;
        private Set<String> rsList;

        private static class JTreeOffset {
            Tree tree;
            int startOffset;
            int endOffset;
            
        public JTreeOffset(Tree tree) {
            this.tree = tree;
            startOffset = -1;
            endOffset = -1;
        }
        public void updateStartOffset(int value) {
            if (value != -1 && (startOffset == -1 || value < startOffset)) {
                startOffset = value;
            }
        }
        public void updateEndOffset(int value) {
            if (value != -1 && (endOffset == -1 || value > endOffset)) {
                endOffset = value;
            }
            
        }
        
        public Tree getTree() {
            return tree;
        }

        public int getStartOffset() {
            return startOffset;
        }

        public int getEndOffset() {
            return endOffset;
        }

        }
        Stack<JTreeOffset> treeStack = new Stack<JTreeOffset>(); 
        
        public BlocksVisitor (JavacTaskImpl jt, CompilationUnitTree cu, JavaFileManager manager, javax.tools.JavaFileObject sibling) {
            assert jt != null;
            assert cu != null;
            assert manager != null;
            assert sibling != null;
            this.jt = jt;
            this.errorName = Name.Table.instance(jt.getContext()).error;
            this.types = com.sun.tools.javac.code.Types.instance(jt.getContext());
            this.cu = cu;
            this.manager = manager;
            this.sibling = sibling;
            this.sourceName = this.manager.inferBinaryName(StandardLocation.SOURCE_PATH, this.sibling);
            this.trees = Trees.instance(jt);
        }
        
        final Types getTypes() {
            return types;
        }
//         @Override public Void scan(Tree node, Blocks p) {
//            if (node == null) {
//                return null;
//            }
//            if (!treeStack.isEmpty()) {
//              JTreeOffset offset = treeStack.lastElement();  
//              offset.updateStartOffset(getStartPosition(getCurrentPath()));
//              offset.updateEndOffset(getEndPosition(getCurrentPath()));
//            }
//            super.scan (node,p);
//            return null;
//        }        
         @Override public Void visitParameterizedType(ParameterizedTypeTree node, final Blocks p) {
            scan(node.getType(), p);
            scan(node.getTypeArguments(), p);
            return null;
        }
        
//        void dump(TypeElement clazz, String className, Element enclosingElement) {
//            PrintWriter output = null;
//            if (this.rsList != null) {
//                this.rsList.add (className);
//            }
//            try {
//                JavaFileObject jfo = manager.getJavaFileForOutput(StandardLocation.CLASS_OUTPUT, className, JavaFileObject.Kind.CLASS, sibling);
//            } catch (IOException e) {
//                log(e);
//            } finally {
//                if (output != null) {
//                    output.close();
//                }
//            }
//        }
        
         @Override public Void visitClass (final ClassTree node, final Blocks p) {
            final ClassSymbol sym = ((JCTree.JCClassDecl)node).sym;
            boolean errorInDecl = false;
            boolean errorIgnorSubtree = true;
            String className = null;
            if (sym != null) {
                errorInDecl = hasErrorName(sym);               
                if (!errorInDecl) {
                    final StringBuilder classNameBuilder = new StringBuilder ();
                    ClassFileUtil.encodeClassName(sym, classNameBuilder, '.');  //NOI18N
                    className = classNameBuilder.toString();
                    final String classNameType = classNameBuilder.toString();                                        
                    errorIgnorSubtree = false;
                }
                
            }
            if (!errorIgnorSubtree) {
                Element old = enclosingElement;
                try {
                    List<? extends Tree> members = node.getMembers();
                    JTreeOffset offset = new JTreeOffset(node);
                    
                    treeStack.push(offset);
//                    scan(node.getModifiers(), p);
//                    scan(node.getTypeParameters(), p);
//                    scan(node.getExtendsClause(), p);
//                    scan(node.getImplementsClause(), p);
//                    scan(node.getMembers(), p);
                    super.visitClass(node, p);
//                    TreePath path = getCurrentPath();
//                    addBlockItem(getStartPosition(path),getEndPosition(path) , p, BlocksItem.CLASS);
                    
                    if (offset != treeStack.pop()) {
                        throw new IllegalStateException("Broken stack: " + node);
                    }
                    
                    addBlockItem(offset.getStartOffset(),offset.getEndOffset() , p, BlocksItem.CLASS);
                } finally {
                    enclosingElement = old;
                }
            }
            return null;
        }
         @Override public Void visitErroneous(final  ErroneousTree tree, Blocks p) {
            List<? extends Tree> trees = tree.getErrorTrees();
            for (Tree t : trees) {
                this.scan(t,p);
            }
            return null;
        }
         
        public Void visitMethod(MethodTree node, Blocks p) {
            Element old = enclosingElement;
            BlockTree bn = node.getBody();
            TreePath path = TreePath.getPath(cu, bn);
            treeStack.lastElement().updateStartOffset(getStartPosition(path));
            addBlockItem(getStartPosition(path), getEndPosition(path),p,BlocksItem.METHOD);
            return super.visitBlock(bn,p);
//            try {
//                enclosingElement = ((JCMethodDecl) node).sym;
//                return super.visitMethod(node, p);
//            } finally {
//                enclosingElement = old;
//            }
        }
        
        private boolean hasErrorName (Symbol cs) {
            while (cs != null) {
                if (cs.name == errorName) {
                    return true;
                }
                cs = cs.getEnclosingElement();
            }
            return false;
        }        
        
        private static String encodeClassName (final Symbol sym) {
            TypeElement toEncode = null;
            if (sym instanceof Symbol.ClassSymbol) {
                final TypeMirror  type = ((Symbol.ClassSymbol)sym).asType();

                if (sym.getEnclosingElement().getKind() == ElementKind.TYPE_PARAMETER) {                
                    if (type.getKind() == TypeKind.ARRAY) {
                        TypeMirror ctype = ((ArrayType) type).getComponentType();
                        if (ctype.getKind() == TypeKind.DECLARED) {
                            toEncode = (TypeElement)((DeclaredType)ctype).asElement();
                        }
                    }
                }
                else {
                    toEncode = (TypeElement) sym;
                }
            }
            return toEncode == null ? null : ClassFileUtil.encodeClassName(toEncode);
        }
        public void log(Exception e) {
            logger.log(Level.SEVERE, null, e);
        }  
        
        public void log (String msg) {
            logger.fine(msg);
        }
        
        public int getStartPosition(TreePath node) {
            return (int) trees.getSourcePositions().getStartPosition(node.getCompilationUnit(), node.getLeaf());
        }
        
        public int getEndPosition(TreePath node) {
            return (int)trees.getSourcePositions().getEndPosition(node.getCompilationUnit(), node.getLeaf());
        }
    
    @Override
    public Void visitBlock(BlockTree tree, Blocks blocks) {
        int startPos = getStartPosition(getCurrentPath());
        int endPos = getEndPosition(getCurrentPath());
        addBlockItem(startPos,endPos,blocks,BlocksItem.BLOCK);
        return super.visitBlock(tree, blocks);
    }

    @Override
    public Void visitDoWhileLoop(DoWhileLoopTree tree, Blocks blocks) {
        Tree st = tree.getStatement();
        addBlockItem(getStartPosition(TreePath.getPath(cu, st)),getEndPosition(TreePath.getPath(cu,st)),blocks,BlocksItem.DO_WHILE_LOOP);
        return super.visitDoWhileLoop(tree, blocks);
    }

    @Override
    public Void visitWhileLoop(WhileLoopTree tree, Blocks blocks) {
        Tree st = tree.getStatement();
                
        addBlockItem(getStartPosition(TreePath.getPath(cu, st)),getEndPosition(TreePath.getPath(cu, st)),blocks,BlocksItem.WHILE_LOOP);
        return super.visitWhileLoop(tree, blocks);
    }

    @Override
    public Void visitForLoop(ForLoopTree tree, Blocks blocks) {
        Tree st = tree.getStatement();
        TreePath path = TreePath.getPath(cu, st);
        addBlockItem(getStartPosition(path),getEndPosition(path),blocks,BlocksItem.FOR_LOOP);
        return super.visitForLoop(tree, blocks);
    }

 
    @Override
    public Void visitSwitch(SwitchTree tree, Blocks blocks) {
        JTreeOffset offset = new JTreeOffset(tree);
       
        treeStack.push(offset);
        Void v = super.visitSwitch(tree, blocks);
        offset.updateStartOffset(getStartPosition(TreePath.getPath(cu,tree)));
        offset.updateEndOffset(getEndPosition(TreePath.getPath(cu,tree)));
        if (offset != treeStack.pop()) {
            throw new IllegalStateException("Broken treeStack: " + tree);
        }
        addBlockItem(offset.getStartOffset(), offset.getEndOffset(), blocks,BlocksItem.SWITCH);
        return v;
    }

    @Override
    public Void visitImport(ImportTree tree, Blocks arg1) {
        updateOffsets(tree);
        return super.visitImport(tree, arg1);
    }

    @Override
    public Void visitVariable(VariableTree tree, Blocks arg1) {
        updateOffsets(tree);
        return super.visitVariable(tree, arg1);
    }

    @Override
    public Void visitEmptyStatement(EmptyStatementTree tree, Blocks arg1) {
        updateOffsets(tree);
        return super.visitEmptyStatement(tree, arg1);
    }

    @Override
    public Void visitEnhancedForLoop(EnhancedForLoopTree arg0, Blocks arg1) {
        return super.visitEnhancedForLoop(arg0, arg1);
    }

    @Override
    public Void visitLabeledStatement(LabeledStatementTree tree, Blocks arg1) {
        updateOffsets(tree);
        return super.visitLabeledStatement(tree, arg1);
    }

    @Override
    public Void visitConditionalExpression(ConditionalExpressionTree tree,
                                           Blocks arg1) {
        updateOffsets(tree);
        return super.visitConditionalExpression(tree, arg1);
    }

    @Override
    public Void visitExpressionStatement(ExpressionStatementTree tree,
                                         Blocks arg1) {
        updateOffsets(tree);
        return super.visitExpressionStatement(tree, arg1);
    }

    @Override
    public Void visitBreak(BreakTree tree, Blocks arg1) {
        updateOffsets(tree);
        return super.visitBreak(tree, arg1);
    }

    @Override
    public Void visitContinue(ContinueTree tree, Blocks arg1) {
        updateOffsets(tree);
        return super.visitContinue(tree, arg1);
    }

    @Override
    public Void visitReturn(ReturnTree tree, Blocks arg1) {
        updateOffsets(tree);
        return super.visitReturn(tree, arg1);
    }

    @Override
    public Void visitThrow(ThrowTree tree, Blocks arg1) {
        updateOffsets(tree);
        return super.visitThrow(tree, arg1);
    }

    @Override
    public Void visitAssert(AssertTree tree, Blocks arg1) {
        updateOffsets(tree);
        return super.visitAssert(tree, arg1);
    }

    @Override
    public Void visitMethodInvocation(MethodInvocationTree tree, Blocks arg1) {
        updateOffsets(tree);
        return super.visitMethodInvocation(tree, arg1);
    }

    @Override
    public Void visitNewClass(NewClassTree tree, Blocks arg1) {
        updateOffsets(tree);
        return super.visitNewClass(tree, arg1);
    }

    @Override
    public Void visitNewArray(NewArrayTree tree, Blocks arg1) {
        updateOffsets(tree);
        return super.visitNewArray(tree, arg1);
    }

    @Override
    public Void visitParenthesized(ParenthesizedTree tree, Blocks arg1) {
        updateOffsets(tree);
        return super.visitParenthesized(tree, arg1);
    }

    @Override
    public Void visitAssignment(AssignmentTree tree, Blocks arg1) {
        updateOffsets(tree);
        return super.visitAssignment(tree, arg1);
    }

    @Override
    public Void visitCompoundAssignment(CompoundAssignmentTree tree, Blocks arg1) {
        updateOffsets(tree);
        return super.visitCompoundAssignment(tree, arg1);
    }

    @Override
    public Void visitUnary(UnaryTree tree, Blocks arg1) {
        updateOffsets(tree);
        return super.visitUnary(tree, arg1);
    }

    @Override
    public Void visitBinary(BinaryTree tree, Blocks arg1) {
        updateOffsets(tree);
        return super.visitBinary(tree, arg1);
    }

    @Override
    public Void visitTypeCast(TypeCastTree tree, Blocks arg1) {
        updateOffsets(tree);
        return super.visitTypeCast(tree, arg1);
    }

    @Override
    public Void visitInstanceOf(InstanceOfTree tree, Blocks arg1) {
        updateOffsets(tree);
        return super.visitInstanceOf(tree, arg1);
    }

    @Override
    public Void visitArrayAccess(ArrayAccessTree tree, Blocks arg1) {
        updateOffsets(tree);
        return super.visitArrayAccess(tree, arg1);
    }

    @Override
    public Void visitMemberSelect(MemberSelectTree tree, Blocks arg1) {
        updateOffsets(tree);
        return super.visitMemberSelect(tree, arg1);
    }

    @Override
    public Void visitIdentifier(IdentifierTree tree, Blocks arg1) {
        updateOffsets(tree);
        return super.visitIdentifier(tree, arg1);
    }

    @Override
    public Void visitLiteral(LiteralTree tree, Blocks arg1) {
        updateOffsets(tree);
        return super.visitLiteral(tree, arg1);
    }

    @Override
    public Void visitPrimitiveType(PrimitiveTypeTree tree, Blocks arg1) {
        updateOffsets(tree);
        return super.visitPrimitiveType(tree, arg1);
    }

    @Override
    public Void visitArrayType(ArrayTypeTree tree, Blocks arg1) {
        updateOffsets(tree);
        return super.visitArrayType(tree, arg1);
    }

    @Override
    public Void visitTypeParameter(TypeParameterTree tree, Blocks arg1) {
        updateOffsets(tree);
        return super.visitTypeParameter(tree, arg1);
    }

    @Override
    public Void visitWildcard(WildcardTree tree, Blocks arg1) {
        updateOffsets(tree);
        return super.visitWildcard(tree, arg1);
    }

    @Override
    public Void visitModifiers(ModifiersTree tree, Blocks arg1) {
        updateOffsets(tree);
        return super.visitModifiers(tree, arg1);
    }

    @Override
    public Void visitAnnotation(AnnotationTree tree, Blocks arg1) {
        updateOffsets(tree);
        return super.visitAnnotation(tree, arg1);
    }

    @Override
    public Void visitOther(Tree tree, Blocks arg1) {
        updateOffsets(tree);
        return super.visitOther(tree, arg1);
    }

    @Override
    public Void visitCase(CaseTree tree, Blocks blocks) {
        
        JTreeOffset offset = new JTreeOffset(tree);
        offset.updateStartOffset(getStartPosition(TreePath.getPath(cu,tree)));
        offset.updateEndOffset(getEndPosition(TreePath.getPath(cu,tree)));
        int size = tree.getStatements().size();
        if (size > 0) {
            offset.updateEndOffset(getEndPosition(TreePath.getPath(cu,tree.getStatements().get(size -1))));
        }         
        Void v =  super.visitCase(tree, blocks);
        addBlockItem(offset.getStartOffset(),offset.getEndOffset(),blocks,BlocksItem.CASE);
        return v;
    }

    @Override
    public Void scan(TreePath path, Blocks arg1) {
        if (!treeStack.isEmpty()) {
            treeStack.lastElement().updateStartOffset(getStartPosition(path));
            treeStack.lastElement().updateEndOffset(getEndPosition(path));
        }
        return super.scan(path, arg1);
    }

    @Override
    public Void visitTry(TryTree tree, Blocks blocks) {
        Tree bt = tree.getBlock();
        addBlockItem(getStartPosition(getCurrentPath()),getEndPosition(getCurrentPath()), blocks, BlocksItem.TRY);
        return super.visitTry(tree, blocks);
    }

    @Override
    public Void visitCatch(CatchTree tree, Blocks blocks) {
        Tree bt = tree.getBlock();
        addBlockItem(getStartPosition(getCurrentPath()),getEndPosition(getCurrentPath()), blocks, BlocksItem.CATCH);
        return super.visitCatch(tree, blocks);
    }

    @Override
    public Void visitIf(IfTree tree, Blocks blocks) {
        StatementTree st = tree.getThenStatement();
        
        TreePath path = TreePath.getPath(cu, st);
        addBlockItem(getStartPosition(path),getEndPosition(path),blocks,BlocksItem.IF);
        st = tree.getElseStatement();
        if (st != null) {
            path = TreePath.getPath(cu, st);        
            addBlockItem(getStartPosition(path),getEndPosition(path),blocks,BlocksItem.ELSE);
        }
        return super.visitIf(tree, blocks);
    }
    
    @Override
    public Void visitSynchronized(SynchronizedTree arg0, Blocks arg1) {
        return super.visitSynchronized(arg0, arg1);
    }
    
    
 private void addBlockItem(int startPost,int endPos,Blocks b, BlocksItem item) {
    b.addBlockItem(startPost, endPos,item);
    if (!treeStack.isEmpty()) {
       treeStack.lastElement().updateStartOffset(startPost);
       treeStack.lastElement().updateEndOffset(endPos);
    }
 }

 private void updateOffsets(Tree tree) {
        TreePath path = TreePath.getPath(cu, tree);
        if (!treeStack.isEmpty()) {
           treeStack.lastElement().updateStartOffset(getStartPosition(path));
           treeStack.lastElement().updateEndOffset(getEndPosition(path));
        }
 }
} 

    