package esjc.ast;

import esjc.parser.ExtendedStaticJavaBaseVisitor;
import esjc.parser.ExtendedStaticJavaParser.*;
import org.antlr.v4.runtime.ParserRuleContext;
import org.eclipse.jdt.core.dom.*;

import java.util.HashMap;
import java.util.List;

/**
 * This class builds JDT AST from ANTLR Parse Tree produced by
 * ExtendedStaticJava parser.
 *
 * @author <a href="mailto:robby@cis.ksu.edu">Robby</a>
 */
public class ExtendedStaticJavaASTBuilder extends
ExtendedStaticJavaBaseVisitor<ASTNode> {

  static CompilationUnit ast(final CompilationUnitContext ctx) {
    final ExtendedStaticJavaASTBuilder builder = new ExtendedStaticJavaASTBuilder();
    return builder.build(ctx);
  }

  final static HashMap<String, InfixExpression.Operator> binopMap;

  final static HashMap<String, PrefixExpression.Operator> unopMap;

  final static HashMap<String, PostfixExpression.Operator> incdecMap;

  static {
    binopMap = new HashMap<String, InfixExpression.Operator>(16);
    ExtendedStaticJavaASTBuilder.binopMap.put(
        "||",
        InfixExpression.Operator.CONDITIONAL_OR);
    ExtendedStaticJavaASTBuilder.binopMap.put(
        "&&",
        InfixExpression.Operator.CONDITIONAL_AND);
    ExtendedStaticJavaASTBuilder.binopMap.put(
        "==",
        InfixExpression.Operator.EQUALS);
    ExtendedStaticJavaASTBuilder.binopMap.put(
        "!=",
        InfixExpression.Operator.NOT_EQUALS);
    ExtendedStaticJavaASTBuilder.binopMap.put(
        "==",
        InfixExpression.Operator.EQUALS);
    ExtendedStaticJavaASTBuilder.binopMap.put(
        "<",
        InfixExpression.Operator.LESS);
    ExtendedStaticJavaASTBuilder.binopMap.put(
        ">",
        InfixExpression.Operator.GREATER);
    ExtendedStaticJavaASTBuilder.binopMap.put(
        "<=",
        InfixExpression.Operator.LESS_EQUALS);
    ExtendedStaticJavaASTBuilder.binopMap.put(
        ">=",
        InfixExpression.Operator.GREATER_EQUALS);
    ExtendedStaticJavaASTBuilder.binopMap.put(
        "+",
        InfixExpression.Operator.PLUS);
    ExtendedStaticJavaASTBuilder.binopMap.put(
        "-",
        InfixExpression.Operator.MINUS);
    ExtendedStaticJavaASTBuilder.binopMap.put(
        "*",
        InfixExpression.Operator.TIMES);
    ExtendedStaticJavaASTBuilder.binopMap.put(
        "/",
        InfixExpression.Operator.DIVIDE);
    ExtendedStaticJavaASTBuilder.binopMap.put(
        "%",
        InfixExpression.Operator.REMAINDER);
    ExtendedStaticJavaASTBuilder.binopMap.put("|", InfixExpression.Operator.OR);
    ExtendedStaticJavaASTBuilder.binopMap
    .put("^", InfixExpression.Operator.XOR);
    ExtendedStaticJavaASTBuilder.binopMap
    .put("&", InfixExpression.Operator.AND);
    ExtendedStaticJavaASTBuilder.binopMap.put(
        "<<",
        InfixExpression.Operator.LEFT_SHIFT);
    ExtendedStaticJavaASTBuilder.binopMap.put(
        ">>",
        InfixExpression.Operator.RIGHT_SHIFT_SIGNED);
    ExtendedStaticJavaASTBuilder.binopMap.put(
        ">>>",
        InfixExpression.Operator.RIGHT_SHIFT_UNSIGNED);

    unopMap = new HashMap<String, PrefixExpression.Operator>(4);
    ExtendedStaticJavaASTBuilder.unopMap.put(
        "+",
        PrefixExpression.Operator.PLUS);
    ExtendedStaticJavaASTBuilder.unopMap.put(
        "-",
        PrefixExpression.Operator.MINUS);
    ExtendedStaticJavaASTBuilder.unopMap
    .put("!", PrefixExpression.Operator.NOT);
    ExtendedStaticJavaASTBuilder.unopMap.put(
        "~",
        PrefixExpression.Operator.COMPLEMENT);

    incdecMap = new HashMap<String, PostfixExpression.Operator>(2);
    ExtendedStaticJavaASTBuilder.incdecMap.put(
        "++",
        PostfixExpression.Operator.INCREMENT);
    ExtendedStaticJavaASTBuilder.incdecMap.put(
        "--",
        PostfixExpression.Operator.DECREMENT);
  }

  protected AST ast = AST.newAST(AST.JLS8);

  private ExtendedStaticJavaASTBuilder() {
  }

  @SuppressWarnings("unchecked")
  private void add(@SuppressWarnings("rawtypes") final List l, final Object o) {
    l.add(o);
  }

  @SuppressWarnings("unchecked")
  private <T extends ASTNode> T build(final ParserRuleContext tree) {
    return (T) visit(tree);
  }

  private <E extends ParserRuleContext> void builds(
      @SuppressWarnings("rawtypes") final List l, final List<E> trees) {
    if (trees != null) {
      for (final E e : trees) {
        add(l, build(e));
      }
    }
  }

  @Override
  public InfixExpression visitBinaryExp(final BinaryExpContext ctx) {
    final InfixExpression result = this.ast.newInfixExpression();

    result.setLeftOperand(this.build(ctx.e1));

    result.setOperator(ExtendedStaticJavaASTBuilder.binopMap.get(ctx.op
        .getText()));

    result.setRightOperand(this.build(ctx.e2));

    return result;
  }

  @Override
  public PrimitiveType visitBooleanType(final BooleanTypeContext ctx) {
    return this.ast.newPrimitiveType(PrimitiveType.BOOLEAN);
  }

  @Override
  public TypeDeclaration visitClassDefinition(final ClassDefinitionContext ctx) {
    final TypeDeclaration result = this.ast.newTypeDeclaration();
    add(
        result.modifiers(),
        this.ast.newModifier(Modifier.ModifierKeyword.PUBLIC_KEYWORD));

    result.setName(this.ast.newSimpleName(ctx.ID().getText()));

    add(
        result.bodyDeclarations(),
        this.<MethodDeclaration> build(ctx.mainMethodDeclaration()));

    final List<MemberDeclarationContext> memberDeclarations = ctx
        .memberDeclaration();
    if (memberDeclarations != null) {
      builds(result.bodyDeclarations(), memberDeclarations);
    }

    return result;
  }

  @Override
  public CompilationUnit visitCompilationUnit(final CompilationUnitContext ctx) {
    final CompilationUnit result = this.ast.newCompilationUnit();

    add(result.types(), this.<TypeDeclaration> build(ctx.classDefinition()));

    return result;
  }

  @Override
  public BooleanLiteral visitFalseLiteral(final FalseLiteralContext ctx) {
    return this.ast.newBooleanLiteral(false);
  }

  @Override
  public SimpleName visitIdExp(final IdExpContext ctx) {
    return this.ast.newSimpleName(ctx.ID().getText());
  }

  @Override
  public NumberLiteral visitIntLiteral(final IntLiteralContext ctx) {
    final NumberLiteral result = this.ast.newNumberLiteral();
    result.setToken(ctx.getText());
    return result;
  }

  @Override
  public PrimitiveType visitIntType(final IntTypeContext ctx) {
    return this.ast.newPrimitiveType(PrimitiveType.INT);
  }

  @Override
  public MethodInvocation visitInvoke(final InvokeContext ctx) {
    final MethodInvocation result = this.ast.newMethodInvocation();

    if (ctx.id1 != null) {
      result.setExpression(this.ast.newSimpleName(ctx.id1.getText()));
    }

    result.setName(this.ast.newSimpleName(ctx.id2.getText()));

    final ArgsContext args = ctx.args();
    if (args != null) {
      builds(result.arguments(), args.exp());
    }

    return result;
  }

  @Override
  public MethodInvocation visitInvokeExp(final InvokeExpContext ctx) {
    return this.build(ctx.invoke());
  }

  @Override
  public ExpressionStatement visitInvokeExpStatement(
      final InvokeExpStatementContext ctx) {
    return this.ast.newExpressionStatement(this.<MethodInvocation> build(ctx
        .invoke()));
  }

  @Override
  public VariableDeclarationStatement visitLocalDeclaration(
      final LocalDeclarationContext ctx) {
    final VariableDeclarationFragment vdf = this.ast
        .newVariableDeclarationFragment();
    final VariableDeclarationStatement result = this.ast
        .newVariableDeclarationStatement(vdf);

    result.setType(this.build(ctx.type()));

    vdf.setName(this.ast.newSimpleName(ctx.ID().getText()));

    return result;
  }

  @Override
  public MethodDeclaration visitMainMethodDeclaration(
      final MainMethodDeclarationContext ctx) {
    final MethodDeclaration result = this.ast.newMethodDeclaration();
    add(
        result.modifiers(),
        this.ast.newModifier(Modifier.ModifierKeyword.PUBLIC_KEYWORD));
    add(
        result.modifiers(),
        this.ast.newModifier(Modifier.ModifierKeyword.STATIC_KEYWORD));
    result.setReturnType2(this.ast.newPrimitiveType(PrimitiveType.VOID));
    result.setName(this.ast.newSimpleName("main"));

    final SingleVariableDeclaration svd = this.ast
        .newSingleVariableDeclaration();
    svd.setType(this.ast.newArrayType(this.ast.newSimpleType(this.ast
        .newSimpleName("String"))));
    svd.setName(this.ast.newSimpleName(ctx.id3.getText()));
    add(result.parameters(), svd);

    result.setBody(this.build(ctx.methodBody()));

    return result;
  }

  @Override
  public Block visitMethodBody(final MethodBodyContext ctx) {
    final Block result = this.ast.newBlock();

    final List<LocalDeclarationContext> localDeclarations = ctx
        .localDeclaration();
    if (localDeclarations != null) {
      builds(result.statements(), localDeclarations);
    }

    final List<StatementContext> statements = ctx.statement();
    if (statements != null) {
      builds(result.statements(), statements);
    }

    return result;
  }

  @Override
  public MethodDeclaration visitMethodDeclaration(
      final MethodDeclarationContext ctx) {
    final MethodDeclaration result = this.ast.newMethodDeclaration();
    add(
        result.modifiers(),
        this.ast.newModifier(Modifier.ModifierKeyword.STATIC_KEYWORD));

    result.setReturnType2(this.build(ctx.returnType()));

    result.setName(this.ast.newSimpleName(ctx.ID().getText()));

    final ParamsContext params = ctx.params();
    if (params != null) {
      builds(result.parameters(), params.param());
    }

    result.setBody(this.build(ctx.methodBody()));

    return result;
  }

  @Override
  public Type visitNonVoidReturnType(final NonVoidReturnTypeContext ctx) {
    return this.build(ctx.type());
  }

  @Override
  public NullLiteral visitNullLiteral(final NullLiteralContext ctx) {
    return this.ast.newNullLiteral();
  }

  @Override
  public SingleVariableDeclaration visitParam(final ParamContext ctx) {
    final SingleVariableDeclaration result = this.ast
        .newSingleVariableDeclaration();

    result.setType(this.build(ctx.type()));

    result.setName(this.ast.newSimpleName(ctx.ID().getText()));

    return result;
  }

  @Override
  public ReturnStatement visitReturnStatement(final ReturnStatementContext ctx) {
    final ReturnStatement result = this.ast.newReturnStatement();

    final ExpContext exp = ctx.exp();
    if (exp != null) {
      result.setExpression(this.build(exp));
    }

    return result;
  }

  @Override
  public BooleanLiteral visitTrueLiteral(final TrueLiteralContext ctx) {
    return this.ast.newBooleanLiteral(true);
  }

  @Override
  public PrefixExpression visitUnaryExp(final UnaryExpContext ctx) {
    final PrefixExpression result = this.ast.newPrefixExpression();

    result.setOperator(ExtendedStaticJavaASTBuilder.unopMap.get(ctx.op
        .getText()));

    result.setOperand(this.build(ctx.exp()));

    return result;
  }

  @Override
  public PrimitiveType visitVoidType(final VoidTypeContext ctx) {
    return this.ast.newPrimitiveType(PrimitiveType.VOID);
  }

  @Override
  public WhileStatement visitWhileStatement(final WhileStatementContext ctx) {
    final WhileStatement result = this.ast.newWhileStatement();
    final Block whileBody = this.ast.newBlock();
    result.setBody(whileBody);

    result.setExpression(this.build(ctx.exp()));

    final List<StatementContext> statements = ctx.statement();
    if (statements != null) {
      builds(whileBody.statements(), statements);
    }

    return result;
  }
}
