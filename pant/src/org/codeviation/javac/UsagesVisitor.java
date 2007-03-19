/*
 * UsagesVisitor.java
 *
 * Created on October 29, 2006, 9:02 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.codeviation.javac;
import com.sun.source.tree.ClassTree;
import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.ErroneousTree;
import com.sun.source.tree.IdentifierTree;
import com.sun.source.tree.MemberSelectTree;
import com.sun.source.tree.MethodInvocationTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.NewClassTree;
import com.sun.source.tree.ParameterizedTypeTree;
import com.sun.source.tree.Tree;
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
import org.netbeans.modules.java.source.usages.ClassIndexImpl;

/**
 *Based on UsageVisitor from retouche.
 * @author pzajac
 */
    public  class UsagesVisitor extends TreeScanner<Void,Usages> {
        static final Logger logger = Logger.getLogger(UsagesVisitor.class.getName());
        private Trees trees;
        enum State {EXTENDS, IMPLEMENTS, GT, OTHER};
        
        private final Stack<String> activeClass;
        private JavaFileManager manager;
        private final JavacTaskImpl jt;
        private final Name errorName;
        private final CompilationUnitTree cu;        
        private final Types types;
        private final javax.tools.JavaFileObject sibling;
        private final String sourceName;
        private State state;        
        private Element enclosingElement = null;
        private Set<String> rsList;
        
        
        public UsagesVisitor (JavacTaskImpl jt, CompilationUnitTree cu, JavaFileManager manager, javax.tools.JavaFileObject sibling) {
            assert jt != null;
            assert cu != null;
            assert manager != null;
            assert sibling != null;
            this.activeClass = new Stack<String> ();
            this.jt = jt;
            this.errorName = Name.Table.instance(jt.getContext()).error;
            this.state = State.OTHER;
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
        
        public @Override Void scan(Tree node, Usages p) {
            if (node == null) {
                return null;
            }
            super.scan (node,p);
            return null;
        }        
        
        public @Override Void visitMemberSelect(final MemberSelectTree node,  final Usages p) {
            handleVisitIdentSelect (node,((JCTree.JCFieldAccess)node).sym, p);
            State oldState = this.state;
            this.state = State.OTHER;
            Void ret = super.visitMemberSelect (node, p);
            this.state = oldState;
            return ret;
        }

        public @Override Void visitIdentifier(final IdentifierTree node, final Usages p) {
            handleVisitIdentSelect (node,((JCTree.JCIdent)node).sym, p);
            return super.visitIdentifier(node, p);
        }
        
        private void handleVisitIdentSelect (Tree tree,Symbol sym, Usages p) {
            log("handlerVisitIdentSelect");
            if (!activeClass.empty()) {               
                if (sym != null) {
                    if (sym.getKind().isClass() || sym.getKind().isInterface()) {
                        final String className = encodeClassName(sym);
                        if (className != null) {
                            int sp = getStartPosition(tree);
                            int ep = getEndPosition(tree);
                            switch (this.state) {
                                case EXTENDS:
                                    p.addExtends(className, sp, ep);
                                    addUsage(activeClass.peek(),className, p, ClassIndexImpl.UsageType.SUPER_CLASS);
                                    break;
                                case IMPLEMENTS:
                                    p.addImplements(className, sp, ep);
                                    addUsage (activeClass.peek(),className,p, ClassIndexImpl.UsageType.SUPER_INTERFACE);
                                    break;
                                case OTHER:
                                case GT:
                                    addUsage (activeClass.peek(),className,p, ClassIndexImpl.UsageType.TYPE_REFERENCE);
                                    break;
                            }
                        }
                    }
                    else if (sym.getKind().isField()) {
                        final Symbol owner = sym.getEnclosingElement();
                        final String className = encodeClassName(owner);
                        if (className != null) {
                            addUsage (activeClass.peek(),className,p,ClassIndexImpl.UsageType.FIELD_REFERENCE);
                        }                        
                    }
                    else if (sym.getKind() == ElementKind.CONSTRUCTOR || sym.getKind() == ElementKind.METHOD) {
                        log("metoda");
                        final Symbol owner = sym.getEnclosingElement();
                        final String className = encodeClassName(owner);
                        if (className != null) {
                            int sp = getStartPosition(tree);
                            int ep = getEndPosition(tree);
                            p.addUsage(sym.toString(),className,sp,ep);
                            addUsage (activeClass.peek(),className,p,ClassIndexImpl.UsageType.METHOD_REFERENCE);
                        }                        
                    }
                }
            }
        }
        
        public @Override Void visitParameterizedType(ParameterizedTypeTree node, final Usages p) {
            scan(node.getType(), p);
            State currState = this.state;
            this.state = State.GT;
            scan(node.getTypeArguments(), p);
            this.state = currState;
            return null;
        }
        
        void dump(TypeElement clazz, String className, Element enclosingElement) {
            PrintWriter output = null;
            if (this.rsList != null) {
                this.rsList.add (className);
            }
            try {
                JavaFileObject jfo = manager.getJavaFileForOutput(StandardLocation.CLASS_OUTPUT, className, JavaFileObject.Kind.CLASS, sibling);
            } catch (IOException e) {
                log(e);
            } finally {
                if (output != null) {
                    output.close();
                }
            }
        }
        
        protected boolean shouldGenerate (final String binaryName, ClassSymbol sym) {
            if (binaryName == null) {
                return false;
            }
            if  (sym.getQualifiedName().isEmpty()) {
                return true;
            }        
            Symbol enclosing = sym.getEnclosingElement();
            while (enclosing != null && enclosing.getKind() != ElementKind.PACKAGE) {
                if (!enclosing.getKind().isClass() && !enclosing.getKind().isInterface()) {
                    return true;
                }
                enclosing = enclosing.getEnclosingElement();
            }
            return false;
    }
        
        public @Override Void visitClass (final ClassTree node, final Usages p) {
            final ClassSymbol sym = ((JCTree.JCClassDecl)node).sym;
            boolean errorInDecl = false;
            boolean errorIgnorSubtree = true;
            String className = null;
            if (sym != null) {
                errorInDecl = hasErrorName(sym);               
                if (errorInDecl) {
                    if (activeClass.size()>0) {
                        activeClass.push (activeClass.get(0));
                        errorIgnorSubtree = false;
                    }
                    else {
                        if (this.cu instanceof JCTree.JCCompilationUnit) {
//                            JavaFileObject jfo = ((JCTree.JCCompilationUnit)this.cu).sourcefile;
//                            if (jfo != null) {
//                                URI uri = jfo.toUri();
//                                if (uri != null && uri.isAbsolute()) {
//                                    try {
//                                        FileObject fo = URLMapper.findFileObject(uri.toURL());
//                                        if (fo != null) {
//                                            ClassPath cp = ClassPath.getClassPath(fo,ClassPath.SOURCE);
//                                            if (cp != null) {
//                                                className = cp.getResourceName(fo,'.',false);
//                                            }
//                                        }
//                                    } catch (MalformedURLException e) {
//                                        log(e);
//                                    }
//                                }
//                            }
                        }                   
                        if (className != null) {
                            final String classNameType = className ;//+ DocumentUtil.encodeKind(ElementKind.CLASS);
                            activeClass.push (classNameType);
                            errorIgnorSubtree = false;
                            addUsage (classNameType,className, p, ClassIndexImpl.UsageType.TYPE_REFERENCE);
                        }
                        else {
//                            Logger.getLogger("global").warning(String.format("Cannot resolve %s, ignoring whole subtree.\n",sym.toString()));    //NOI18N
                        }
                    }
                }
                else {
                    
                    final StringBuilder classNameBuilder = new StringBuilder ();
                    ClassFileUtil.encodeClassName(sym, classNameBuilder, '.');  //NOI18N
                    className = classNameBuilder.toString();
//                    classNameBuilder.append(DocumentUtil.encodeKind(sym.getKind()));
                    final String classNameType = classNameBuilder.toString();                                        
                    if (activeClass.size() == 0 && !className.equals(sourceName)) {
                        rsList = new HashSet<String>();
                    }
                    activeClass.push (classNameType);
                    errorIgnorSubtree = false;
                    addUsage (classNameType,className, p, ClassIndexImpl.UsageType.TYPE_REFERENCE);
                }
                
            }
            if (!errorIgnorSubtree) {
                Element old = enclosingElement;
                try {
                    enclosingElement = sym;
                    scan(node.getModifiers(), p);
                    scan(node.getTypeParameters(), p);
                    state = errorInDecl ? State.OTHER : State.EXTENDS;
                    scan(node.getExtendsClause(), p);
                    state = errorInDecl ? State.OTHER : State.IMPLEMENTS;
                    scan(node.getImplementsClause(), p);
                    state = State.OTHER;
                    scan(node.getMembers(), p);
                    activeClass.pop();
                } finally {
                    enclosingElement = old;
                }
            }
            if (shouldGenerate(className, sym)) {
                dump(sym, className, enclosingElement);
            }
            return null;
        }
        
        public @Override Void visitNewClass(NewClassTree node, Usages p) {
            final Symbol sym = ((JCTree.JCNewClass)node).constructor;                        
            if (sym != null) {
                final Symbol owner = sym.getEnclosingElement();
                if (owner != null && owner.getKind().isClass()) {
                    final String className = encodeClassName(owner);
                    if (className != null) { 
                        int sp = getStartPosition(node);
                        int ep = getEndPosition(node);
                         p.addUsage(sym.toString(),className,sp,ep);
                        addUsage(activeClass.peek(),className,p,ClassIndexImpl.UsageType.METHOD_REFERENCE);
                    }
                }                
            }
            return super.visitNewClass (node,p);
        }       
        
        public @Override Void visitErroneous(final  ErroneousTree tree, Usages p) {
            List<? extends Tree> trees = tree.getErrorTrees();
            for (Tree t : trees) {
                this.scan(t,p);
            }
            return null;
        }
         
        public Void visitMethod(MethodTree node, Usages p) {
            Element old = enclosingElement;
            try {
                enclosingElement = ((JCMethodDecl) node).sym;
                return super.visitMethod(node, p);
            } finally {
                enclosingElement = old;
            }
        }
        public @Override Void visitMethodInvocation(MethodInvocationTree tree,Usages p) {
            log("MethodInovation");
            return super.visitMethodInvocation(tree,p);
        }
        private void addUsage (final String ownerName, final String className, final Usages map, final ClassIndexImpl.UsageType type) {
//            assert className != null;
//            assert map != null;
//            assert type != null;
//            Map<String,Set<ClassIndexImpl.UsageType>> tUsages = map.get(ownerName);
//            if (tUsages == null) {
//                tUsages = new HashMap<String,Set<ClassIndexImpl.UsageType>> ();
//                map.put(ownerName,tUsages);
//            }
//            Set<ClassIndexImpl.UsageType> usageType = tUsages.get (className);
//            if (usageType == null) {
//                usageType = EnumSet.noneOf(ClassIndexImpl.UsageType.class);
//                tUsages.put (className, usageType);
//            }
//            usageType.add (type);        
            log("add usage:" + ownerName + "," + className  );
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
        
        public int getStartPosition(Tree node) {
            return (int) trees.getSourcePositions().getStartPosition(cu, node);
        }
        
        public int getEndPosition(Tree node) {
            return (int)trees.getSourcePositions().getEndPosition(cu, node);
        }
    } 

    