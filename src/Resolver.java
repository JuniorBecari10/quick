import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

public class Resolver implements Stmt.StmtVisitor<Void>, Expr.ExprVisitor<Object> {
  private final Interpreter interpreter;
  private final Stack<Map<String, Boolean>> scopes = new Stack<>();

  public Resolver(Interpreter interpreter) {
    this.interpreter = interpreter;
  }

  public void resolve(List<Stmt> statements) {
    for (Stmt stmt : statements) {
      try {
        resolve(stmt);
      } catch (Exception e) {
        continue;
      }
    }
  }

  public void resolve(Stmt stmt) throws Exception {
    stmt.accept(this);
  }

  public void resolve(Expr expr) throws Exception {
    expr.accept(this);
  }

  public void resolveLocal(Expr expr, Token name) {
    for (int i = scopes.size() - 1; i >= 0; i--) {
      if (scopes.get(i).containsKey(name.lexeme())) {
        interpreter.resolve(expr, scopes.size() - 1 - i);
        return;
      }
    }
  }

  // ---

  private void beginScope() {
    scopes.push(new HashMap<>());
  }

  private void endScope() {
    scopes.pop();
  }

  // ---

  private void declare(Token name) {
    if (scopes.isEmpty()) return;

    Map<String, Boolean> scope = scopes.peek();
    scope.put(name.lexeme(), false);
  }

  private void define(Token name) {
    if (scopes.isEmpty()) return;
    scopes.peek().put(name.lexeme(), true);
  }

  // ---

  @Override
  public Void visitBlockStmt(Stmt.BlockStmt stmt) throws Exception {
    beginScope();
    resolve(stmt.statements);
    endScope();

    return null;
  }

  @Override
  public Void visitBreakStmt(Stmt.BreakStmt stmt) throws Exception {
    throw new UnsupportedOperationException("Unimplemented method 'visitBreakStmt'");
  }

  @Override
  public Void visitContinueStmt(Stmt.ContinueStmt stmt) throws Exception {
    throw new UnsupportedOperationException("Unimplemented method 'visitContinueStmt'");
  }

  @Override
  public Void visitExprStmt(Stmt.ExprStmt stmt) throws Exception {
    throw new UnsupportedOperationException("Unimplemented method 'visitExprStmt'");
  }

  @Override
  public Void visitFnStmt(Stmt.FnStmt stmt) throws Exception {
    beginScope();
    
    for (Token param : stmt.params) {
      declare(param);
      define(param);
    }

    resolve(stmt.body);
    endScope();

    return null;
  }

  @Override
  public Void visitIfStmt(Stmt.IfStmt stmt) throws Exception {
    throw new UnsupportedOperationException("Unimplemented method 'visitIfStmt'");
  }

  @Override
  public Void visitLetStmt(Stmt.LetStmt stmt) throws Exception {
    throw new UnsupportedOperationException("Unimplemented method 'visitLetStmt'");
  }

  @Override
  public Void visitLoopStmt(Stmt.LoopStmt stmt) throws Exception {
    throw new UnsupportedOperationException("Unimplemented method 'visitLoopStmt'");
  }

  @Override
  public Void visitReturnStmt(Stmt.ReturnStmt stmt) throws Exception {
    throw new UnsupportedOperationException("Unimplemented method 'visitReturnStmt'");
  }

  @Override
  public Void visitWhileStmt(Stmt.WhileStmt stmt) throws Exception {
    throw new UnsupportedOperationException("Unimplemented method 'visitWhileStmt'");
  }

  // ---

  @Override
  public Object visitArrayExpr(Expr.ArrayExpr expr) throws Exception {
    throw new UnsupportedOperationException("Unimplemented method 'visitArrayExpr'");
  }

  @Override
  public Object visitAssignExpr(Expr.AssignExpr expr) throws Exception {
    resolve(expr.value);
    resolveLocal(expr, expr.name);
    return null;
  }

  @Override
  public Object visitAssignIndexExpr(Expr.AssignIndexExpr expr) throws Exception {
    throw new UnsupportedOperationException("Unimplemented method 'visitAssignIndexExpr'");
  }

  @Override
  public Object visitBinaryExpr(Expr.BinaryExpr expr) throws Exception {
    throw new UnsupportedOperationException("Unimplemented method 'visitBinaryExpr'");
  }

  @Override
  public Object visitCallExpr(Expr.CallExpr expr) throws Exception {
    throw new UnsupportedOperationException("Unimplemented method 'visitCallExpr'");
  }

  @Override
  public Object visitFnExpr(Expr.FnExpr expr) throws Exception {
    throw new UnsupportedOperationException("Unimplemented method 'visitFnExpr'");
  }

  @Override
  public Object visitGroupingExpr(Expr.GroupingExpr expr) throws Exception {
    throw new UnsupportedOperationException("Unimplemented method 'visitGroupingExpr'");
  }

  @Override
  public Object visitIndexExpr(Expr.ArrayIndexExpr expr) throws Exception {
    throw new UnsupportedOperationException("Unimplemented method 'visitIndexExpr'");
  }

  @Override
  public Object visitLiteralExpr(Expr.LiteralExpr expr) throws Exception {
    throw new UnsupportedOperationException("Unimplemented method 'visitLiteralExpr'");
  }

  @Override
  public Object visitRangeExpr(Expr.RangeExpr expr) throws Exception {
    throw new UnsupportedOperationException("Unimplemented method 'visitRangeExpr'");
  }

  @Override
  public Object visitTernaryExpr(Expr.TernaryExpr expr) throws Exception {
    throw new UnsupportedOperationException("Unimplemented method 'visitTernaryExpr'");
  }

  @Override
  public Object visitUnaryExpr(Expr.UnaryExpr expr) throws Exception {
    throw new UnsupportedOperationException("Unimplemented method 'visitUnaryExpr'");
  }

  @Override
  public Object visitVariableExpr(Expr.IdentifierExpr expr) throws Exception {
    if (!scopes.isEmpty() && scopes.peek().get(expr.name.lexeme()) == Boolean.FALSE) {
      Util.printError("Cannot read local variable in its own initializer", expr.pos);
    }

    resolveLocal(expr, expr.name);
    return null;
  }
}
