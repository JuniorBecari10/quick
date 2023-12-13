import java.util.List;

public interface FnDecl {
  List<Token> params();
  List<Stmt> body();
}
