public class Ref {
  public final Token name;
  public final Environment env;

  public Ref(Token name, Environment env) {
    this.name = name;
    this.env = env;
  }

  public Object getReferenced() throws Exception {
    return this.env.get(this.name);
  }

  @Override
  public String toString() {
    try {
      return "&" + Util.stringify(this.getReferenced());
    } catch (Exception e) {
      try {
        Util.printError("Variable '" + name.lexeme() + "' doesn't exist in this or a parent scope", name.pos());
      } catch (Exception e1) {}
    }

    // expected this not to happen
    return "";
  }
}
