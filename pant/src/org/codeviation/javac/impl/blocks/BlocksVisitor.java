
package org.codeviation.javac.impl.blocks;
import com.sun.source.tree.BlockTree;
import com.sun.source.tree.CaseTree;
import com.sun.source.tree.CatchTree;
import com.sun.source.tree.DoWhileLoopTree;
import com.sun.source.tree.ForLoopTree;
import com.sun.source.tree.IfTree;
import com.sun.source.tree.SwitchTree;
import com.sun.source.tree.SynchronizedTree;
import com.sun.source.tree.TryTree;
import com.sun.source.tree.WhileLoopTree;
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
import com.sun.source.util.TreeScanner;
import com.sun.source.util.Trees;
import com.sun.tools.javac.api.JavacTaskImpl;
import com.sun.tools.javac.code.Symbol;
import com.sun.tools.javac.code.Symbol.ClassSymbol;
import com.sun.tools.javac.code.Type.ArrayType;
import com.sun.tools.javac.code.Types;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.JCTree.JCMethodDecl;
import com.sun.tools.javac.util.Name;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashSet;
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
import javax.tools.JavaFileObject;
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
        super.visitDoWhileLoop(tree, blocks);
        addBlockItem(getStartPosition(TreePath.getPath(cu, st)),getEndPosition(TreePath.getPath(cu,st)),blocks,BlocksItem.DO_WHILE_LOOP);
        return null;
    }

    @Override
    public Void visitWhileLoop(WhileLoopTree tree, Blocks blocks) {
        Tree st = tree.getStatement();
        super.visitWhileLoop(tree, blocks);        
        addBlockItem(getStartPosition(getCurrentPath()),getEndPosition(getCurrentPath()),blocks,BlocksItem.WHILE_LOOP);
        return null;
    }

    @Override
    public Void visitForLoop(ForLoopTree tree, Blocks blocks) {
        Tree st = tree.getStatement();
        TreePath path = TreePath.getPath(cu, st);
        addBlockItem(getStartPosition(path),getEndPosition(path),blocks,BlocksItem.FOR_LOOP);
        super.visitForLoop(tree, blocks);
        return null;
    }

    @Override
    public Void visitSwitch(SwitchTree tree, Blocks blocks) {
        JTreeOffset offset = new JTreeOffset(tree);
        treeStack.push(offset);
        Void v = super.visitSwitch(tree, blocks);
        if (offset != treeStack.pop()) {
            throw new IllegalStateException("Broken treeStack: " + tree);
        }
        addBlockItem(offset.getStartOffset(), offset.getEndOffset(), blocks,BlocksItem.SWITCH);
        return v;
    }

    @Override
    public Void visitCase(CaseTree tree, Blocks blocks) {
        
        JTreeOffset offset = new JTreeOffset(tree);
        offset.updateStartOffset(getStartPosition(getCurrentPath()));
        offset.updateEndOffset(getEndPosition(getCurrentPath()));
        treeStack.push(offset);
        Void v =  super.visitCase(tree, blocks);
        if (offset != treeStack.pop()) {
            throw new IllegalStateException("Broken treeStack : " + tree);
        }
        addBlockItem(offset.getStartOffset(),offset.getEndOffset(),blocks,BlocksItem.CASE);
        return v;
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
//        path = TreePath.getPath(cu, st);        
//        addBlockItem(getStartPosition(path),getEndPosition(path),blocks,BlocksItem.ELSE);
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

} 

    