import java.util.ArrayList;
import java.util.List;

public class Interpreter implements Stmt.StmtVisitor<Void>, Expr.ExprVisitor<Object> {
  public final Environment globals = new Environment();
  private Environment environment = globals;
  
  private List<Stmt> statements;

  public Interpreter(List<Stmt> statements) {
    this.statements = statements;

    globals.define("clock", new Callable() {
      public int arity() { return 0; }

      public Object call(Interpreter interpreter, List<Object> args) {
        return (double) System.currentTimeMillis() / 1000.0;
      }

      public String toString() { return "<native fn>"; }
    });

    globals.define("println", new Callable() {
      public int arity() { return 1; }

      public Object call(Interpreter interpreter, List<Object> args) {
        System.out.println(Util.stringify(args.get(0)));
        return null;
      }

      public String toString() { return "<native fn>"; }
    });

    globals.define("print", new Callable() {
      public int arity() { return 1; }

      public Object call(Interpreter interpreter, List<Object> args) {
        System.out.print(Util.stringify(args.get(0)));
        return null;
      }

      public String toString() { return "<native fn>"; }
    });

    globals.define("input", new Callable() {
      public int arity() { return 1; }

      public Object call(Interpreter interpreter, List<Object> args) {
        System.out.print(Util.stringify(args.get(0)));
        return System.console().readLine();
      }

      public String toString() { return "<native fn>"; }
    });
  }

  public void interpret() throws Exception {
    for (Stmt stmt : this.statements) {
      try {
        this.execute(stmt);
      }
      catch (Util.Break b) {
        Util.printError("Cannot use 'break' outside a loop", stmt.pos);
      }
      catch (Util.Continue c) {
        Util.printError("Cannot use 'continue' outside a loop", stmt.pos);
      }
      catch (Util.Return r) {
        Util.printError("Cannot use 'return' outside a function", stmt.pos);
      }
    }
  }

  // ---

  private void execute(Stmt stmt) throws Exception {
    stmt.accept(this);
  }

  public void executeBlock(List<Stmt> statements, Environment environment) throws Exception {
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

  // ---
  
  private boolean isTruthy(Object obj) {
    if (obj == null) return false;
    if (obj instanceof Boolean) return (boolean) obj;

    return true;
  }

  private boolean isEqual(Object a, Object b) {
    if (a == null && b == null) return true;
    if (a == null) return false;

    return a.equals(b);
  }

  private void checkNumberOperand(Token operator, Object operand) throws Exception {
    if (operand instanceof Double) return;
    Util.printError("Operand must be a number", operator.pos());
  }

  private void checkNumberOperands(Token operator, Object left, Object right) throws Exception {
    if (left instanceof Double && right instanceof Double) return;
    Util.printError("Operands must be numbers", operator.pos());
  }

  /*
  private Object lookUpVariable(Token name, Expr expr) throws Exception {
    return this.environment.get(name);
  }

  private String stringify(Object object) {
    if (object == null) return "nil";

    if (object instanceof Double) {
        String text = object.toString();
        
        if (text.endsWith(".0")) {
            text = text.substring(0, text.length() - 2);
        }
        
        return text;
    }

    return object.toString();
  }
  */

  // ---

  @Override
  public Void visitBlockStmt(Stmt.BlockStmt stmt) throws Exception {
    this.executeBlock(stmt.statements, this.environment);
    return null;
  }

  @Override
  public Void visitBreakStmt(Stmt.BreakStmt stmt) throws Exception {
    throw new Util.Break();
  }

  @Override
  public Void visitContinueStmt(Stmt.ContinueStmt stmt) throws Exception {
    throw new Util.Continue();
  }

  @Override
  public Void visitExprStmt(Stmt.ExprStmt stmt) throws Exception {
    this.evaluate(stmt.expr);
    return null;
  }

  @Override
  public Void visitFnStmt(Stmt.FnStmt stmt) throws Exception {
    Function fn = new Function(stmt, this.environment);
    this.environment.define(stmt.name.lexeme(), fn);

    return null;
  }

  @Override
  public Void visitIfStmt(Stmt.IfStmt stmt) throws Exception {
    if (this.isTruthy(this.evaluate(stmt.condition)))
      this.execute(stmt.thenBranch);
    else if (stmt.elseBranch != null)
      this.execute(stmt.elseBranch);
    
    return null;
  }

  @Override
  public Void visitLetStmt(Stmt.LetStmt stmt) throws Exception {
    Object value = this.evaluate(stmt.value);

    if (this.environment.containsVariable(stmt.name.lexeme()))
      Util.printError("Cannot redeclare variable '" + stmt.name.lexeme() + "'", stmt.name.pos());

    this.environment.define(stmt.name.lexeme(), value);

    return null;
  }

  @Override
  public Void visitLoopStmt(Stmt.LoopStmt stmt) throws Exception {
    if (stmt.variable == null || stmt.iterable == null) {
      while (true) {
        try {
          this.execute(stmt.block);
        }
        catch (Util.Break b) {
          break;
        }
        catch (Util.Continue c) {
          continue;
        }
      }

      return null;
    }

    Object iterable = this.evaluate(stmt.iterable);

    if (!(iterable instanceof Iterable))
      Util.printError("Can only iterate over iterable objects", stmt.pos);

    Iterable it = (Iterable) iterable;

    Environment previous = this.environment;
    this.environment = new Environment(previous);

    while (it.hasNext()) {
      try {
        this.environment.define(stmt.variable.lexeme(), it.next());
        this.execute(stmt.block);
      }
      catch (Util.Break b) {
        break;
      }
      catch (Util.Continue c) {
        continue;
      }
    }

    this.environment = previous;
    return null;
  }

  @Override
  public Void visitReturnStmt(Stmt.ReturnStmt stmt) throws Exception {
    Object value = null;

    if (stmt.value != null)
      value = this.evaluate(stmt.value);

    throw new Util.Return(value);
  }

  @Override
  public Void visitWhileStmt(Stmt.WhileStmt stmt) throws Exception {
    while (this.isTruthy(this.evaluate(stmt.condition))){
      try {
        this.execute(stmt.block);
      }
      catch (Util.Break b) {
        break;
      }
      catch (Util.Continue c) {
        continue;
      }
    }

    return null;
  }

  // ---

  private Object evaluate(Expr expr) throws Exception {
    return expr.accept(this);
  }

  private Object plus(Object left, Object right) {
    if (left instanceof Double && right instanceof Double)
      return (double) left + (double) right;
      
    return Util.stringify(left) + Util.stringify(right);
  }

  private Object minus(Object left, Object right) {
    return (double) left - (double) right;
  }

  private Object times(Object left, Object right) {
    return (double) left * (double) right;
  }

  private Object divide(Object left, Object right) {
    return (double) left <= (double) right;
  }

  private Object performOperation(Object left, Object right, Token operator) {
    switch (operator.type()) {
      case PlusEqual:
        return this.plus(left, right);

      case MinusEqual:
        this.checkNumberOperands(expr.operator, left, right);
        return this.minus(left, right);

      case StarEqual:
        this.checkNumberOperands(expr.operator, left, right);
        return this.times(left, right);

      case SlashEqual:
        this.checkNumberOperands(expr.operator, left, right);

        if ((double) right == 0.0)
          Util.printError("Cannot divide by zero", expr.left.pos);

        return this.divide(left, right);
      
      default:
        Util.printError("Invalid assignment operator: '" + operator.lexeme() + "'", operator.pos());
    }
  }

  // ---

  @Override
  public Object visitArrayExpr(Expr.ArrayExpr expr) throws Exception {
    List<Object> array = new ArrayList<>();

    for (Expr e : expr.items)
      array.add(this.evaluate(e));

    return new Array(array);
  }

  @Override
  public Object visitAssignExpr(Expr.AssignExpr expr) throws Exception {
    Object value = this.evaluate(expr.value);

    if (expr.isRef) {
      Object v = this.environment.get(expr.name);

      if (!(v instanceof Ref))
        Util.printError("Can only dereference assign ref objects", expr.name.pos());
      
      Ref r = (Ref) v;
      value = this.performOperation(value, expr.operator);
      r.env.assign(r.name, value);
    }
    else
      this.environment.assign(expr.name, value);

    return value;
  }

  @Override
  public Object visitAssignIndexExpr(Expr.AssignIndexExpr expr) throws Exception {
    Object value = this.evaluate(expr.value);
    Object index = this.evaluate(expr.index);

    if (!(index instanceof Double))
      Util.printError("Arrays can only be indexed by integers", expr.pos);
    
    Double ind = (Double) index;
    
    if (ind.intValue() != ind)
      Util.printError("Arrays can only be indexed by integers", expr.pos);
    
    this.environment.assignArray(expr.name, ind.intValue(), value);
    return value;
  }

  @Override
  public Object visitBinaryExpr(Expr.BinaryExpr expr) throws Exception {
    Object left = this.evaluate(expr.left);
    Object right = this.evaluate(expr.right);

    switch (expr.operator.type()) {
      case DoubleEqual:
        return this.isEqual(left, right);
    
      case BangEqual:
        return !this.isEqual(left, right);

      // ---

      case Ampersand:
        return this.isTruthy(left) && this.isTruthy(right);

      case VerticalBar:
        return this.isTruthy(left) || this.isTruthy(right);

      // ---

      case Greater:
        this.checkNumberOperands(expr.operator, left, right);
        return (double) left > (double) right;

      case GreaterEqual:
        this.checkNumberOperands(expr.operator, left, right);
        return (double) left >= (double) right;

      case Less:
        this.checkNumberOperands(expr.operator, left, right);
        return (double) left < (double) right;

      case LessEqual:
        this.checkNumberOperands(expr.operator, left, right);
        return (double) left <= (double) right;

      // ---

      case Plus:
        return this.plus(left, right);

      case Minus:
        this.checkNumberOperands(expr.operator, left, right);
        return this.minus(left, right);

      case Star:
        this.checkNumberOperands(expr.operator, left, right);
        return this.times(left, right);

      case Slash:
        this.checkNumberOperands(expr.operator, left, right);

        if ((double) right == 0.0)
          Util.printError("Cannot divide by zero", expr.left.pos);

        return this.divide(left, right);

      // ---

      case InKw:
        if (!(right instanceof Array))
          Util.printError("Right side of the 'in' expression must be an array", expr.left.pos);
        
        Array a = (Array) right;

        for (Object o : a.array)
          if (left.equals(o))
            return true;

        return false;

      default:
        Util.printError("Invalid binary operator: '" + expr.operator.lexeme() + "'", expr.left.pos);
        return null;
    }
  }

  @Override
  public Object visitCallExpr(Expr.CallExpr expr) throws Exception {
    Object callee = this.evaluate(expr.callee);

    List<Object> args = new ArrayList<>();
    
    for (Expr arg : expr.args)
      args.add(this.evaluate(arg));
    
    if (!(callee instanceof Callable))
      Util.printError("Can only call functions", expr.callee.pos);

    Callable function = (Callable) callee;

    if (args.size() != function.arity())
      Util.printError("Expected " + function.arity() + " arguments, but got " + args.size() + " instead", expr.callee.pos);

    return function.call(this, args);
  }

  @Override
  public Object visitFnExpr(Expr.FnExpr expr) throws Exception {
    return new Function(expr, this.environment);
  }

  @Override
  public Object visitGroupingExpr(Expr.GroupingExpr expr) throws Exception {
    return this.evaluate(expr.expr);
  }

  @Override
  public Object visitIndexExpr(Expr.IndexExpr expr) throws Exception {
    Object array = this.evaluate(expr.array);
    Object index = this.evaluate(expr.index);

    if (!(array instanceof Array))
      Util.printError("Can only index arrays", expr.pos);
    
    if (!(index instanceof Double))
      Util.printError("Arrays can only be indexed by integers", expr.pos);

    Double ind = (Double) index;

    if (ind.intValue() != ind)
      Util.printError("Arrays can only be indexed by integers", expr.pos);

    Array a = (Array) array;

    if (ind < 0 || ind >= a.array.size())
      Util.printError("Index out of bounds: '" + ind + "' is outside the bounds for an array of length " + a.array.size(), expr.pos);
    
    return a.array.get(ind.intValue());
   }

  @Override
  public Object visitLiteralExpr(Expr.LiteralExpr expr) throws Exception {
    return expr.value;
  }

  @Override
  public Object visitRangeExpr(Expr.RangeExpr expr) throws Exception {
    Object start = this.evaluate(expr.start);
    Object end = this.evaluate(expr.end);
    Object step = expr.step == null
                    ? 1.0
                    : this.evaluate(expr.step);

    if (!(start instanceof Double))
      Util.printError("The start of the range must be a number", expr.pos);
    
    if (!(end instanceof Double))
      Util.printError("The end of the range must be a number", expr.pos);
    
    if (!(step instanceof Double))
      Util.printError("The step of the range must be a number: " + Util.stringify(step), expr.pos);
    
    return new Range((Double) start, (Double) end, (Double) step);
  }

  @Override
  public Object visitTernaryExpr(Expr.TernaryExpr expr) throws Exception {
    if (this.isTruthy(this.evaluate(expr.condition)))
      return this.evaluate(expr.thenBranch);
    
    return this.evaluate(expr.elseBranch);
  }

  @Override
  public Object visitUnaryExpr(Expr.UnaryExpr expr) throws Exception {
    Object right = this.evaluate(expr.right);

    switch (expr.operator.type()) {
      case Bang:
        return !this.isTruthy(right);
      
      case Minus:
        this.checkNumberOperand(expr.operator, right);
        return -(double) right;
      
      case Ampersand:
        return new Ref(((Expr.VariableExpr) expr.right).name, this.environment);
      
      case Star:
        if (!(right instanceof Ref))
          Util.printError("Can only dereference ref objects", expr.operator.pos());
        
        Ref r = (Ref) right;
        return r.env.get(r.name);
      
      default:
        Util.printError("Invalid unary operator: '" + expr.operator.lexeme() + "'", expr.operator.pos());
        return null;
    }
  }

  @Override
  public Object visitVariableExpr(Expr.VariableExpr expr) throws Exception {
    return this.environment.get(expr.name);
  }
}
