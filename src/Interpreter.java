import java.util.List;

public class Interpreter implements Stmt.StmtVisitor<Void>, Expr.ExprVisitor<Object> {
  private Environment environment;
  private List<Stmt> statements;

  public Interpreter(List<Stmt> statements) {
    this.environment = new Environment();
    this.statements = statements;
  }

  public void interpret() throws Exception {
    for (Stmt stmt : this.statements)
      this.execute(stmt);
  }

  // ---

  private void execute(Stmt stmt) throws Exception {
    stmt.accept(this);
  }

  private void executeBlock(List<Stmt> statements, Environment environment) throws Exception {
    Environment previous = this.environment;

    try {
      this.environment = environment;

      for (Stmt stmt : statements)
        this.execute(stmt);
    }
    finally {
      this.environment = previous;
    }
  }

  @Override
  public Void visitBlockStmt(Stmt.BlockStmt stmt) throws Exception {
    this.executeBlock(stmt.statements, this.environment);
    return null;
  }

  @Override
  public Void visitBreakStmt(Stmt.BreakStmt stmt) throws Exception {
    // TODO Auto-generated method stub
    throw new UnsupportedOperationException("Unimplemented method 'visitBreakStmt'");
  }

  @Override
  public Void visitContinueStmt(Stmt.ContinueStmt stmt) throws Exception {
    // TODO Auto-generated method stub
    throw new UnsupportedOperationException("Unimplemented method 'visitContinueStmt'");
  }

  @Override
  public Void visitExprStmt(Stmt.ExprStmt stmt) throws Exception {
    // TODO Auto-generated method stub
    throw new UnsupportedOperationException("Unimplemented method 'visitExprStmt'");
  }

  @Override
  public Void visitIfStmt(Stmt.IfStmt stmt) throws Exception {
    // TODO Auto-generated method stub
    throw new UnsupportedOperationException("Unimplemented method 'visitIfStmt'");
  }

  @Override
  public Void visitLetStmt(Stmt.LetStmt stmt) throws Exception {
    Object value = this.evaluate(stmt.value);
    this.environment.define(stmt.name.lexeme(), value);

    return null;
  }

  @Override
  public Void visitLoopStmt(Stmt.LoopStmt stmt) throws Exception {
    // TODO Auto-generated method stub
    throw new UnsupportedOperationException("Unimplemented method 'visitLoopStmt'");
  }

  @Override
  public Void visitReturnStmt(Stmt.ReturnStmt stmt) throws Exception {
    // TODO Auto-generated method stub
    throw new UnsupportedOperationException("Unimplemented method 'visitReturnStmt'");
  }

  @Override
  public Void visitWhileStmt(Stmt.WhileStmt stmt) throws Exception {
    // TODO Auto-generated method stub
    throw new UnsupportedOperationException("Unimplemented method 'visitWhileStmt'");
  }

  // ---

  private Object evaluate(Expr expr) throws Exception {
    return expr.accept(this);
  }

  @Override
  public Object visitAssignExpr(Expr.AssignExpr expr) throws Exception {
    Object value = this.evaluate(expr.value);
    this.environment.assign(expr.name, value);

    return value;
  }

  @Override
  public Object visitBinaryExpr(Expr.BinaryExpr expr) throws Exception {
    // TODO Auto-generated method stub
    throw new UnsupportedOperationException("Unimplemented method 'visitBinaryExpr'");
  }

  @Override
  public Object visitCallExpr(Expr.CallExpr expr) throws Exception {
    // TODO Auto-generated method stub
    throw new UnsupportedOperationException("Unimplemented method 'visitCallExpr'");
  }

  @Override
  public Object visitGroupingExpr(Expr.GroupingExpr expr) throws Exception {
    // TODO Auto-generated method stub
    throw new UnsupportedOperationException("Unimplemented method 'visitGroupingExpr'");
  }

  @Override
  public Object visitLiteralExpr(Expr.LiteralExpr expr) throws Exception {
    // TODO Auto-generated method stub
    throw new UnsupportedOperationException("Unimplemented method 'visitLiteralExpr'");
  }

  @Override
  public Object visitUnaryExpr(Expr.UnaryExpr expr) throws Exception {
    // TODO Auto-generated method stub
    throw new UnsupportedOperationException("Unimplemented method 'visitUnaryExpr'");
  }

  @Override
  public Object visitVariableExpr(Expr.VariableExpr expr) throws Exception {
    return this.environment.get(expr.name);
  }
}
