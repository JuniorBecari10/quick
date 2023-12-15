import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class Parser {
  private final List<Token> tokens;
  private int current;

  public Parser(List<Token> tokens) {
    this.tokens = tokens;
    this.current = 0;
  }

  public List<Stmt> parse() throws Exception {
    List<Stmt> statements = new ArrayList<>();

    while (!this.isAtEnd(0)) {
      Stmt stmt = this.statement();

      if (stmt instanceof Stmt.InclStmt) {
        List<Stmt> add = this.parseInclude(stmt);
        statements.addAll(add);

        continue;
      }

      if (stmt != null)
        statements.add(stmt);
    }

    return statements;
  }

  // ---

  private Stmt statement() throws Exception {
    try {
      this.skipNewLines();

      if (this.isAtEnd(0))
        return null;

      Token t = this.peek(0);

      if (this.check(TokenType.LBrace)) return this.blockStmt(t);
      if (this.match(TokenType.BreakKw)) return this.breakStmt(t);
      if (this.match(TokenType.ContinueKw)) return this.continueStmt(t);
      if (this.match(TokenType.FnKw)) return fnStmt(t);
      if (this.match(TokenType.IfKw)) return ifStmt(t);
      if (this.match(TokenType.InclKw)) return inclStmt(t);
      if (this.match(TokenType.LetKw)) return letStmt(t);
      if (this.match(TokenType.LoopKw)) return loopStmt(t);
      if (this.match(TokenType.ReturnKw)) return returnStmt(t);
      if (this.match(TokenType.WhileKw)) return whileStmt(t);

      return exprStmt(t);
    }
    catch (Exception e) {
      this.synchronize();
      return null;
    }
  }

  // ---

  private Stmt blockStmt(Token t) throws Exception {
    Stmt.BlockStmt stmt = new Stmt.BlockStmt(t.pos(), this.block());

    this.consumeNewLine();
    return stmt;
  }

  private Stmt breakStmt(Token t) throws Exception {
    Stmt.BreakStmt stmt = new Stmt.BreakStmt(t.pos());

    this.consumeNewLine();
    return stmt;
  }

  private Stmt continueStmt(Token t) throws Exception {
    Stmt.ContinueStmt stmt = new Stmt.ContinueStmt(t.pos());

    this.consumeNewLine();
    return stmt;
  }

  private Stmt fnStmt(Token t) throws Exception {
    Token name = this.consume(TokenType.Identifier, "Expected function name");
    this.consume(TokenType.LParen, "Expected '(' after function name");

    List<Token> parameters = new ArrayList<>();

    if (!this.check(TokenType.RParen)) {
      do {
        parameters.add(this.consume(TokenType.Identifier, "Expected parameter name"));
      }
      while (this.match(TokenType.Comma));
    }

    this.consume(TokenType.RParen, "Expected ')' after parameter list");
    
    List<Stmt> body;

      if (this.match(TokenType.Colon)) {
        this.skipNewLines();
        Expr expr = this.expr();

        body = new ArrayList<>();
        body.add(new Stmt.ReturnStmt(expr.pos, expr));
      }
      else
        body = this.block();

    return new Stmt.FnStmt(t.pos(), name, parameters, body);
  }

  // if condition { thenBranch } else/(if condition ...) { elseBranch } 
  private Stmt ifStmt(Token t) throws Exception {
    Expr condition = this.expr();

    Stmt.BlockStmt thenBranch = new Stmt.BlockStmt(this.peek(0).pos(), this.block());
    Stmt.BlockStmt elseBranch = null;

    if (this.match(TokenType.ElseKw)) {
      if (this.match(TokenType.IfKw)) {
        Stmt elseIf = this.ifStmt(this.peek(-1));

        List<Stmt> list = new ArrayList<>();
        list.add(elseIf);
        return new Stmt.IfStmt(t.pos(), condition, thenBranch, new Stmt.BlockStmt(elseIf.pos, list));
      }
      
      elseBranch = new Stmt.BlockStmt(this.peek(0).pos(), this.block());
    }

    return new Stmt.IfStmt(t.pos(), condition, thenBranch, elseBranch);
  }

  private Stmt inclStmt(Token t) throws Exception {
    Token mod = this.consume(TokenType.Identifier, "Expected module name");

    this.consumeNewLine();
    return new Stmt.InclStmt(t.pos(), mod);
  }

  // let name = value
  private Stmt letStmt(Token t) throws Exception {
    Token name = this.consume(TokenType.Identifier, "Expected variable name after 'let', got '" + this.peek(0).lexeme() + "'");
    Expr value = null;

    if (this.match(TokenType.Equal))
      value = this.expr();
    else
      Util.printError("Expected '=' after variable name, got '" + this.peek(0).lexeme() + "'", this.peek(0).pos());
    
    this.consumeNewLine();
    return new Stmt.LetStmt(t.pos(), name, value);
  }

  private Stmt loopStmt(Token t) throws Exception {
    Token variable = null;
    Expr iterable = null;

    if (!this.check(TokenType.LBrace)) {
      variable = this.consume(TokenType.Identifier, "Iterator variable name must be an identifier");

      this.consume(TokenType.InKw, "Expected 'in' keyword after iterator variable");
      iterable = this.expr();
    }

    Stmt.BlockStmt block = new Stmt.BlockStmt(this.peek(0).pos(), this.block());
    return new Stmt.LoopStmt(t.pos(), variable, iterable, block);
  }

  private Stmt returnStmt(Token t) throws Exception {
    Expr value = null;

    if (!this.check(TokenType.NewLine))
      value = this.expr();

    this.consumeNewLine();
    return new Stmt.ReturnStmt(t.pos(), value);
  }

  private Stmt whileStmt(Token t) throws Exception {
    Expr condition = this.expr();
    Stmt.BlockStmt block = new Stmt.BlockStmt(this.peek(0).pos(), this.block());

    return new Stmt.WhileStmt(t.pos(), condition, block);
  }

  private Stmt exprStmt(Token t) throws Exception {
    Expr expr = this.expr();

    this.consumeNewLine();
    return new Stmt.ExprStmt(t.pos(), expr);
  }

  // ---

  private List<Stmt> block() throws Exception {
    List<Stmt> statements = new ArrayList<>();

    if (this.match(TokenType.Arrow)) {
      statements.add(this.statement());
      return statements;
    }

    this.skipNewLines();
    consume(TokenType.LBrace, "Expected '{' before block, got '" + this.peek(0).type() + "'");
    this.skipNewLines();

    while (!this.check(TokenType.RBrace) && !this.isAtEnd(0))
      statements.add(this.statement());
    
    this.skipNewLines();
    this.consume(TokenType.RBrace, "Expected '}' after block, got '" + this.peek(0).type() + "'");
    this.skipNewLines();

    return statements;
  }

  // ---

  private Expr expr() throws Exception {
    return this.parseExpr(0);
  }


  /*
   * Precedence Order:
   * 
   * - Lowest
   * 
   * 0 - Assignment
   * 1 - Or
   * 2 - And
   * 3 - Equality
   * 4 - Comparison
   * 5 - Range
   * 6 - Add, Sub
   * 7 - Mul, Div
   * 8 - Unary
   * 9 - Index
   * 10 - Call
   * 11 - Primary
   * 
   * - Highest
   */
  private Expr parseExpr(int precedence) throws Exception {
    switch (precedence) {
      case 0: return this.assignment(precedence);
      case 1: return this.binary(precedence, TokenType.VerticalBar);
      case 2: return this.binary(precedence, TokenType.Ampersand);
      case 3: return this.binary(precedence, TokenType.DoubleEqual, TokenType.BangEqual);
      case 4: return this.binary(precedence, TokenType.Greater, TokenType.GreaterEqual, TokenType.Less, TokenType.LessEqual);
      case 5: return this.range(precedence);
      case 6: return this.binary(precedence, TokenType.Plus, TokenType.Minus);
      case 7: return this.binary(precedence, TokenType.Star, TokenType.Slash);
      case 8: return this.unary(precedence, TokenType.Bang, TokenType.Minus, TokenType.Ampersand, TokenType.Star);
      case 9: return this.index(precedence);
      case 10: return this.call(precedence);
      case 11: return this.primary();
    }

    Util.printError("Invalid precedence: '" + precedence + "'", null);
    return null;
  }

  private Expr assignment(int precedence) throws Exception {
    Expr expr = this.parseExpr(precedence + 1);

    if (this.match(TokenType.Equal)) {
      Expr right = this.assignment(precedence); // this is because assignment is right-associative

      if (expr instanceof Expr.VariableExpr) {
        Token name = ((Expr.VariableExpr) expr).name;
        return new Expr.AssignExpr(expr.pos, name, right, false);
      }
      
      else if (expr instanceof Expr.UnaryExpr) {
        Expr.UnaryExpr unary = (Expr.UnaryExpr) expr;

        if (unary.operator.type() == TokenType.Star && unary.right instanceof Expr.VariableExpr) {
          Token name = ((Expr.VariableExpr) unary.right).name;
          return new Expr.AssignExpr(expr.pos, name, right, true);
        }
      }

      else if (expr instanceof Expr.IndexExpr) {
        Expr.IndexExpr index = (Expr.IndexExpr) expr;

        if (index.array instanceof Expr.VariableExpr) {
          Token name = ((Expr.VariableExpr) index.array).name;
          return new Expr.AssignIndexExpr(expr.pos, name, index.index, right);
        }
      }
      
      Util.printError("Invalid assignment target", expr.pos);
    }

    return expr;
  }

  private Expr binary(int precedence, TokenType... operators) throws Exception {
    Expr expr = this.parseExpr(precedence + 1);

    while (this.match(operators)) {
      Token operator = this.peek(-1);
      Expr right = this.parseExpr(precedence + 1);
      
      expr = new Expr.BinaryExpr(expr.pos, expr, operator, right);
    }

    return expr;
  }

  private Expr unary(int precedence, TokenType... operators) throws Exception {
    if (this.match(operators)) {
      Token operator = this.peek(-1);
      Expr right = this.unary(precedence, operators); // always will be unary, since the precedence is the same
      
      if ((operator.type() == TokenType.Ampersand || operator.type() == TokenType.Star) && !(right instanceof Expr.VariableExpr))
        Util.printError("Can only reference or dereference identifiers", operator.pos());

      return new Expr.UnaryExpr(operator.pos(), operator, right);
    }

    return this.parseExpr(precedence + 1);
  }

  private Expr range(int precedence) throws Exception {
    Expr expr = this.parseExpr(precedence + 1);

    if (this.match(TokenType.Colon)) {
      Expr right = this.parseExpr(precedence + 1);

      Expr step = null;
      if (this.match(TokenType.Comma))
        step = this.parseExpr(precedence + 1);
      
      expr = new Expr.RangeExpr(expr.pos, expr, right, step);
    }

    return expr;
  }

  private Expr call(int precedence) throws Exception {
    Expr expr = this.parseExpr(precedence + 1);

    while (true) {
      if (this.match(TokenType.LParen)) {
        List<Expr> args = new ArrayList<>();
        
        if (!this.check(TokenType.RParen)) {
          do {
            args.add(this.expr());
          }
          while (this.match(TokenType.Comma));
        }

        this.consume(TokenType.RParen, "Expected ')' to finish function call");
        expr = new Expr.CallExpr(expr.pos, expr, args);
      }
      else
        break;
    }

    return expr;
  }

  private Expr index(int precedence) throws Exception {
    Expr expr = this.parseExpr(precedence + 1);

    if (this.match(TokenType.LBracket)) {
      Expr index = this.expr();

      this.consume(TokenType.RBracket, "Expected ']' after index");
      expr = new Expr.IndexExpr(expr.pos, expr, index);
    }

    return expr;
  }

  private Expr primary() throws Exception {
    Position pos = this.peek(-1).pos();

    if (this.match(TokenType.FalseKw)) return new Expr.LiteralExpr(pos, false);
    if (this.match(TokenType.TrueKw)) return new Expr.LiteralExpr(pos, true);
    if (this.match(TokenType.NilKw)) return new Expr.LiteralExpr(pos, null);

    if (this.match(TokenType.Number, TokenType.String)) return new Expr.LiteralExpr(pos, this.peek(-1).literal());
    if (this.match(TokenType.Identifier))
      return new Expr.VariableExpr(pos, this.peek(-1));

    // if cond: thenBranch else: elseBranch
    if (this.match(TokenType.IfKw)) {
      Expr condition = this.expr();

      this.consume(TokenType.Colon, "Expected ':' after ternary condition");

      Expr thenBranch = this.expr();

      this.skipNewLines();
      this.consume(TokenType.ElseKw, "Ternary operators must have an 'else' clause");
      this.consume(TokenType.Colon, "Expected ':' after 'else'");

      Expr elseBranch = this.expr();
      return new Expr.TernaryExpr(pos, condition, thenBranch, elseBranch);
    }

    if (this.match(TokenType.FnKw)) {
      this.consume(TokenType.LParen, "Expected '(' after 'fn'");

      List<Token> parameters = new ArrayList<>();

      if (!this.check(TokenType.RParen)) {
        do {
          parameters.add(this.consume(TokenType.Identifier, "Expected parameter name"));
        }
        while (this.match(TokenType.Comma));
      }

      this.consume(TokenType.RParen, "Expected ')' after parameter list");

      List<Stmt> body;

      if (this.match(TokenType.Colon)) {
        this.skipNewLines();
        Expr expr = this.expr();

        body = new ArrayList<>();
        body.add(new Stmt.ReturnStmt(expr.pos, expr));
      }
      else
        body = this.block();

      return new Expr.FnExpr(pos, parameters, body);
    }

    if (this.match(TokenType.LBracket)) {
      List<Expr> items = new ArrayList<>();

      if (!this.check(TokenType.RBracket)) {
        do {
          items.add(this.expr());
        }
        while (this.match(TokenType.Comma));
      }

      this.consume(TokenType.RBracket, "Expected ']' after array elements");
      return new Expr.ArrayExpr(pos, items);
    }

    if (this.match(TokenType.LParen)) {
      Expr expr = this.expr();
      
      this.consume(TokenType.RParen, "Expected ')' after expression");
      return new Expr.GroupingExpr(pos, expr);
    }

    Util.printError("Invalid expression: '" + this.peek(0).lexeme() + "'", this.peek(0).pos());
    return null;
  }

  // ---

  private boolean isAtEnd(int offset) {
    return this.current + offset >= this.tokens.size();
  }

  private Token peek(int offset) {
    if (this.isAtEnd(offset)) return null;
    
    return this.tokens.get(this.current + offset);
  }

  private Token advance() {
    Token t = this.peek(0);
    this.current++;

    return t;
  }

  private boolean match(TokenType... types) {
    for (TokenType type : types) {
      if (this.check(type)) {
        this.advance();
        return true;
      }
    }

    return false;
  }

  private boolean check(TokenType t) {
    if (this.isAtEnd(0)) return false;
    return this.peek(0).type() == t;
  }

  private Token consume(TokenType t, String message) throws Exception {
    if (this.check(t)) return this.advance();

    Util.printError(message, this.peek(0).pos());
    return null;
  }

  private Token consumeNewLine() throws Exception {
    if (this.isAtEnd(0)) return null;
    if (this.check(TokenType.RBrace)) return this.peek(0);
    return this.consume(TokenType.NewLine, "Expected new line after statement, got '" + this.peek(0).lexeme() + "'");
  }

  private void skipNewLines() {
    while (!this.isAtEnd(0) && this.check(TokenType.NewLine))
      this.advance();
  }

  // ---

  private List<Stmt> parseInclude(Stmt stmt) throws Exception {
    Stmt.InclStmt incl = (Stmt.InclStmt) stmt;
    File f = new File(incl.mod.lexeme() + Modules.FILE_EXT);

    if (Modules.included.contains(incl.mod.lexeme() + Modules.FILE_EXT))
      Util.printError("Module '" + incl.mod.lexeme() + "' already included", stmt.pos);

    if (!f.exists())
      Util.printError("Module '" + incl.mod.lexeme() + "' doesn't exist", incl.mod.pos());
    
    return Modules.readFile(f);
  }

  private void synchronize() {
    this.advance();

    while (!this.isAtEnd(0)) {
      if (this.peek(-1).type() == TokenType.NewLine) return;

      switch (this.peek(0).type()) {
        case BreakKw,
             ContinueKw,
             IfKw,
             LetKw,
             LoopKw,
             ReturnKw,
             WhileKw:
          return;
        
        default:
          this.advance();
          break;
      }
    }
  }
}
