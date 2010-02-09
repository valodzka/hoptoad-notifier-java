package code.lucamarrocco.hoptoad;

public class ValidBacktrace {
  public static boolean matches(String string) {
    return string.matches("[^:]*:\\d+.*");
  }
}
