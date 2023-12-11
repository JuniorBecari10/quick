public class Ref {
  public final String name;
  public final Environment env;

  public Ref(String name, Environment env) {
    this.name = name;
    this.env = env;
  }

  @Override
  public String toString() {
    return "&" + this.name;
  }
}
