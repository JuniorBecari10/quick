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
      Stmt stmt = this.statement(true);

      if (stmt != null)
        statements.add(stmt);
    }

    return statements;
  }

  // ---

  private Stmt statement(boolean requireNewLine) throws Exception {
    try {
      this.skipNewLines();

      if (this.isAtEnd(0))
        return null;

      Token t = this.peek(0);

      if (this.check(TokenType.LBrace)) return this.blockStmt(t);
      if (this.match(TokenType.BreakKw)) return this.breakStmt(t, requireNewLine);
      if (this.match(TokenType.ContinueKw)) return this.continueStmt(t, requireNewLine);
      if (this.match(TokenType.FnKw)) return this.fnStmt(t);
      if (this.match(TokenType.IfKw)) return this.ifStmt(t);
      if (this.match(TokenType.LetKw)) return this.letStmt(t, requireNewLine);
      if (this.match(TokenType.LoopKw)) return this.loopStmt(t);
      if (this.match(TokenType.ReturnKw)) return this.returnStmt(t, requireNewLine);
      if (this.match(TokenType.WhileKw)) return this.whileStmt(t);

      return exprStmt(t, requireNewLine);
    }
    catch (Exception e) {
      this.synchronize();
      return null;
    }
  }

  // ---

  // { stmts }
  private Stmt blockStmt(Token t) throws Exception {
    Stmt.BlockStmt stmt = new Stmt.BlockStmt(t.pos(), this.block());

    return stmt;
  }

  // break
  private Stmt breakStmt(Token t, boolean requireNewLine) throws Exception {
    Stmt.BreakStmt stmt = new Stmt.BreakStmt(t.pos());

    if (requireNewLine)
      this.consumeNewLine();
    return stmt;
  }

  // continue
  private Stmt continueStmt(Token t, boolean requireNewLine) throws Exception {
    Stmt.ContinueStmt stmt = new Stmt.ContinueStmt(t.pos());

    if (requireNewLine)
      this.consumeNewLine();
    return stmt;
  }

  // fn name([params]) { body }
  private Stmt fnStmt(Token t) throws Exception {
    Token name = this.consume(TokenType.Identifier, "Expected function name, got '" + this.peek(0).lexeme() + "'");
    this.consume(TokenType.LParen, "Expected '(' after function name, got '" + this.peek(0).lexeme() + "'");

    List<Token> parameters = new ArrayList<>();

    if (!this.check(TokenType.RParen)) {
      do {
        this.skipNewLines();
        parameters.add(this.consume(TokenType.Identifier, "Expected parameter name, got '" + this.peek(0).lexeme() + "'"));
      }
      while (this.match(TokenType.Comma));
    }

    this.consume(TokenType.RParen, "Expected ')' after parameter list, got '" + this.peek(0).lexeme() + "'");
    
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

  // if condition { thenBranch } [else/(if condition ...) { elseBranch }]
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

  // let name [= value]
  private Stmt letStmt(Token t, boolean requireNewLine) throws Exception {
    Token name = this.consume(TokenType.Identifier, "Expected variable name after 'let', got '" + this.peek(0).lexeme() + "'");
    Expr value = null;

    if (this.isAtEnd(0) || this.match(TokenType.NewLine))
      return new Stmt.LetStmt(t.pos(), name, new Expr.LiteralExpr(this.peek(-1).pos(), null));

    if (this.match(TokenType.Equal))
      value = this.expr();
    else
      Util.printError("Expected '=' after variable name, got '" + this.peek(0).lexeme() + "'", this.peek(0).pos());
    
    if (requireNewLine)
      this.consumeNewLine();
    return new Stmt.LetStmt(t.pos(), name, value);
  }

  // loop [i in iterator] { block }
  private Stmt loopStmt(Token t) throws Exception {
    Token variable = null;
    Expr iterable = null;

    if (!this.check(TokenType.LBrace)) {
      variable = this.consume(TokenType.Identifier, "Iterator variable name must be an identifier, got '" + this.peek(0).lexeme() + "'");

      this.consume(TokenType.InKw, "Expected 'in' keyword after iterator variable, got '" + this.peek(0).lexeme() + "'");
      iterable = this.expr();
    }

    Stmt.BlockStmt block = new Stmt.BlockStmt(this.peek(0).pos(), this.block());
    return new Stmt.LoopStmt(t.pos(), variable, iterable, block);
  }

  // return [value]
  private Stmt returnStmt(Token t, boolean requireNewLine) throws Exception {
    Expr value = null;

    if (!this.check(TokenType.NewLine))
      value = this.expr();

    if (requireNewLine)
      this.consumeNewLine();
    return new Stmt.ReturnStmt(t.pos(), value);
  }

  // while condition { block }
  private Stmt whileStmt(Token t) throws Exception {
    Expr condition = this.expr();
    Stmt.BlockStmt block = new Stmt.BlockStmt(this.peek(0).pos(), this.block());

    return new Stmt.WhileStmt(t.pos(), condition, block);
  }

  // expr
  private Stmt exprStmt(Token t, boolean requireNewLine) throws Exception {
    Expr expr = this.expr();

    if (requireNewLine)
      this.consumeNewLine();
    return new Stmt.ExprStmt(t.pos(), expr);
  }

  // ---

  private List<Stmt> block() throws Exception {
    List<Stmt> statements = new ArrayList<>();
    this.skipNewLines();

    if (this.match(TokenType.Arrow)) {
      this.skipNewLines();

      statements.add(this.statement(false));
      return statements;
    }

    this.skipNewLines();
    consume(TokenType.LBrace, "Expected '{' before block, got '" + this.peek(0).type() + "'");
    this.skipNewLines();

    while (!this.check(TokenType.RBrace) && !this.isAtEnd(0)) {
      statements.add(this.statement(true));

      this.skipNewLines();
      if (this.check(TokenType.RBrace) || this.isAtEnd(0))
        break;
    }
    
    this.skipNewLines();
    this.consume(TokenType.RBrace, "Expected '}' after block, got '" + this.peek(0).type() + "'");

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
   * 5 - In
   * 6 - Range
   * 7 - Bit Shift
   * 8 - Add, Sub
   * 9 - Mul, Div, Mod
   * 10 - Postfix
   * 11 - Prefix
   * 12 - Index
   * 13 - Call
   * 14 - Primary
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
      case 5: return this.binary(precedence, TokenType.InKw);
      case 6: return this.range(precedence);
      case 7: return this.binary(precedence, TokenType.LShift, TokenType.RShift);
      case 8: return this.binary(precedence, TokenType.Plus, TokenType.Minus);
      case 9: return this.binary(precedence, TokenType.Star, TokenType.Slash, TokenType.Modulo);
      case 10: return this.postfix(precedence, TokenType.DoublePlus, TokenType.DoubleMinus);
      case 11: return this.prefix(precedence, TokenType.Bang, TokenType.Minus, TokenType.Ampersand, TokenType.Star);
      case 12: return this.index(precedence);
      case 13: return this.call(precedence);
      case 14: return this.primary();
    }

    Util.printError("Invalid precedence: '" + precedence + "'", null);
    return null;
  }

  private Expr assignment(int precedence) throws Exception {
    Expr expr = this.parseExpr(precedence + 1);

    if (this.match(TokenType.Equal, TokenType.PlusEqual, TokenType.MinusEqual, TokenType.StarEqual, TokenType.SlashEqual, TokenType.ModuloEqual, TokenType.LShiftEqual, TokenType.RShiftEqual)) {
      Token operator = this.peek(-1);
      Expr right = this.assignment(precedence); // this is because assignment is right-associative

      if (expr instanceof Expr.IdentifierExpr) {
        Token name = ((Expr.IdentifierExpr) expr).name;
        return new Expr.AssignExpr(expr.pos, name, operator, expr, right, false);
      }
      
      else if (expr instanceof Expr.UnaryExpr) {
        Expr.UnaryExpr unary = (Expr.UnaryExpr) expr;

        if (unary.operator.type() == TokenType.Star && unary.operand instanceof Expr.IdentifierExpr) {
          Token name = ((Expr.IdentifierExpr) unary.operand).name;
          return new Expr.AssignExpr(expr.pos, name, operator, expr, right, true);
        }
      }

      else if (expr instanceof Expr.ArrayIndexExpr) {
        Expr.ArrayIndexExpr index = (Expr.ArrayIndexExpr) expr;

        if (index.array instanceof Expr.IdentifierExpr) {
          Token name = ((Expr.IdentifierExpr) index.array).name;
          return new Expr.AssignIndexExpr(expr.pos, name, operator, index.index, expr, right);
        }
      }
      
      Util.printError("Invalid assignment target: '" + this.peek(0).lexeme() + "'", expr.pos);
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

  private Expr prefix(int precedence, TokenType... operators) throws Exception {
    if (this.match(operators)) {
      Token operator = this.peek(-1);
      Expr right = this.prefix(precedence, operators); // always will be unary, since the precedence is the same
      
      // TODO! accept array index too
      if ((operator.type() == TokenType.Ampersand || operator.type() == TokenType.Star) && !(right instanceof Expr.IdentifierExpr/* || right instanceof Expr.ArrayIndexExpr*/))
        Util.printError("Can only reference or dereference identifiers, got '" + this.peek(0).lexeme() + "'", operator.pos());

      return new Expr.UnaryExpr(operator.pos(), operator, right);
    }

    return this.parseExpr(precedence + 1);
  }

  private Expr postfix(int precedence, TokenType... operators) throws Exception {
    Expr expr = this.parseExpr(precedence + 1);

    if (this.match(operators)) {
      Token operator = this.peek(-1);
      Expr right = new Expr.LiteralExpr(this.peek(-1).pos(), 1.0); // value '1' hardcoded

      if (expr instanceof Expr.IdentifierExpr) {
        Token name = ((Expr.IdentifierExpr) expr).name;
        return new Expr.AssignExpr(expr.pos, name, operator, expr, right, false);
      }
      
      else if (expr instanceof Expr.UnaryExpr) {
        Expr.UnaryExpr unary = (Expr.UnaryExpr) expr;

        if (unary.operator.type() == TokenType.Star && unary.operand instanceof Expr.IdentifierExpr) {
          Token name = ((Expr.IdentifierExpr) unary.operand).name;
          return new Expr.AssignExpr(expr.pos, name, operator, expr, right, true);
        }
      }

      else if (expr instanceof Expr.ArrayIndexExpr) {
        Expr.ArrayIndexExpr index = (Expr.ArrayIndexExpr) expr;

        if (index.array instanceof Expr.IdentifierExpr) {
          Token name = ((Expr.IdentifierExpr) index.array).name;
          return new Expr.AssignIndexExpr(expr.pos, name, operator, index.index, expr, right);
        }
      }
      
      Util.printError("Invalid assignment target: '" + this.peek(0).lexeme() + "'", expr.pos);
    }

    return expr;
  }

  // talvez se a pessoa explicitamente colocar 1 de step lançar um warning falando que é desnecessário
  private Expr range(int precedence) throws Exception {
    Expr expr = this.parseExpr(precedence + 1);

    if (this.match(TokenType.DoubleDot)) {
      Expr right = this.parseExpr(precedence + 1);

      Expr step = null;
      if (this.match(TokenType.Colon))
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
            this.skipNewLines();
            args.add(this.expr());
          }
          while (this.match(TokenType.Comma));
        }

        this.consume(TokenType.RParen, "Expected ')' to finish function call, got '" + this.peek(0).lexeme() + "'");
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

      this.consume(TokenType.RBracket, "Expected ']' after index, got '" + this.peek(0).lexeme() + "'");
      expr = new Expr.ArrayIndexExpr(expr.pos, expr, index);
    }

    return expr;
  }

  private Expr primary() throws Exception {
    Position pos = this.peek(0).pos();

    if (this.match(TokenType.FalseKw)) return new Expr.LiteralExpr(pos, false);
    if (this.match(TokenType.TrueKw)) return new Expr.LiteralExpr(pos, true);
    if (this.match(TokenType.NilKw)) return new Expr.LiteralExpr(pos, null);

    if (this.match(TokenType.Number, TokenType.String)) return new Expr.LiteralExpr(pos, this.peek(-1).literal());
    if (this.match(TokenType.Identifier))
      return new Expr.IdentifierExpr(pos, this.peek(-1));

    // if cond: thenBranch else: elseBranch
    if (this.match(TokenType.IfKw)) {
      Expr condition = this.expr();

      this.consume(TokenType.Colon, "Expected ':' after ternary condition, got '" + this.peek(0).lexeme() + "'");

      Expr thenBranch = this.expr();

      this.skipNewLines();
      this.consume(TokenType.ElseKw, "Ternary operators must have an 'else' clause, got '" + this.peek(0).lexeme() + "'");

      if (this.check(TokenType.IfKw)) {
        Expr elseBranch = this.primary();
        return new Expr.TernaryExpr(pos, condition, thenBranch, elseBranch);
      }
      
      this.consume(TokenType.Colon, "Expected ':' after 'else', got '" + this.peek(0).lexeme() + "'");

      Expr elseBranch = this.expr();
      return new Expr.TernaryExpr(pos, condition, thenBranch, elseBranch);
    }

    if (this.match(TokenType.FnKw)) {
      this.consume(TokenType.LParen, "Expected '(' after 'fn', got '" + this.peek(0).lexeme() + "'");

      List<Token> parameters = new ArrayList<>();

      if (!this.check(TokenType.RParen)) {
        do {
          this.skipNewLines();
          parameters.add(this.consume(TokenType.Identifier, "Expected parameter name, got '" + this.peek(0).lexeme() + "'"));
        }
        while (this.match(TokenType.Comma));
      }

      this.consume(TokenType.RParen, "Expected ')' after parameter list, got '" + this.peek(0).lexeme() + "'");

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
      this.skipNewLines();
      List<Expr> items = new ArrayList<>();

      if (!this.check(TokenType.RBracket)) {
        do {
          this.skipNewLines();
          items.add(this.expr());
        }
        while (this.match(TokenType.Comma));
      }

      this.skipNewLines();
      this.consume(TokenType.RBracket, "Expected ']' after array elements, got '" + this.peek(0).lexeme() + "'");
      return new Expr.ArrayExpr(pos, items);
    }

    if (this.match(TokenType.LParen)) {
      Expr expr = this.expr();
      
      this.consume(TokenType.RParen, "Expected ')' after expression, got '" + this.peek(0).lexeme() + "'");
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
