import java.util.List;

public abstract class Expr {
  public interface ExprVisitor<R> {
    R visitAssignExpr(AssignExpr expr) throws Exception;
    R visitBinaryExpr(BinaryExpr expr) throws Exception;
    R visitCallExpr(CallExpr expr) throws Exception;
    R visitGroupingExpr(GroupingExpr expr) throws Exception;
    R visitLiteralExpr(LiteralExpr expr) throws Exception;
    R visitUnaryExpr(UnaryExpr expr) throws Exception;
    R visitVariableExpr(VariableExpr expr) throws Exception;
  }

  public final Position pos;

  public Expr(Position pos) {
    this.pos = pos;
  }

  public abstract <R> R accept(ExprVisitor<R> visitor) throws Exception;

  public static class AssignExpr extends Expr {
    final Token name;
    final Expr value;
    final boolean isRef;

    public AssignExpr(Position pos, Token name, Expr value, boolean isRef) {
      super(pos);

      this.name = name;
      this.value = value;
      this.isRef = isRef;
    }

    public <R> R accept(ExprVisitor<R> visitor) throws Exception {
      return visitor.visitAssignExpr(this);
    }
  }

  public static class BinaryExpr extends Expr {
    final Expr left;
    final Token operator;
    final Expr right;

    public BinaryExpr(Position pos, Expr left, Token operator, Expr right) {
      super(pos);

      this.left = left;
      this.operator = operator;
      this.right = right;
    }

    public <R> R accept(ExprVisitor<R> visitor) throws Exception {
      return visitor.visitBinaryExpr(this);
    }
  }

  public static class CallExpr extends Expr {
    final Expr callee;
    final List<Expr> args;

    public CallExpr(Position pos, Expr callee, List<Expr> args) {
      super(pos);

      this.callee = callee;
      this.args = args;
    }

    public <R> R accept(ExprVisitor<R> visitor) throws Exception {
      return visitor.visitCallExpr(this);
    }
  }

  public static class GroupingExpr extends Expr {
    final Expr expr;

    public GroupingExpr(Position pos, Expr expr) {
      super(pos);

      this.expr = expr;
    }

    public <R> R accept(ExprVisitor<R> visitor) throws Exception {
      return visitor.visitGroupingExpr(this);
    }
  }

  public static class LiteralExpr extends Expr {
    final Object value;

    public LiteralExpr(Position pos, Object value) {
      super(pos);

      this.value = value;
    }

    public <R> R accept(ExprVisitor<R> visitor) throws Exception {
      return visitor.visitLiteralExpr(this);
    }
  }

  public static class UnaryExpr extends Expr {
    final Token operator;
    final Expr right;

    public UnaryExpr(Position pos, Token operator, Expr right) {
      super(pos);

      this.operator = operator;
      this.right = right;
    }

    public <R> R accept(ExprVisitor<R> visitor) throws Exception {
      return visitor.visitUnaryExpr(this);
    }
  }

  public static class VariableExpr extends Expr {
    final Token name;

    public VariableExpr(Position pos, Token name) {
      super(pos);

      this.name = name;
    }

    public <R> R accept(ExprVisitor<R> visitor) throws Exception {
      return visitor.visitVariableExpr(this);
    }
  }
}
