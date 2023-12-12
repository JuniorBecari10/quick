public class Ref {
  public final Token name;
  public final Environment env;

  public Ref(Token name, Environment env) {
    this.name = name;
    this.env = env;
  }

  @Override
  public String toString() {
    return "&" + this.name;
  }
}
