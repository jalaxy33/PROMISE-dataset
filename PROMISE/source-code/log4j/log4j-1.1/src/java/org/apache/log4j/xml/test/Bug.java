import org.apache.log4j.Category;
import org.apache.log4j.xml.DOMConfigurator;
public class Bug {
  static Category cat = Category.getInstance(Bug.class);
  public static void main( String[] argv) {
    DOMConfigurator.configure(argv[0]);
    cat.debug("Hello world");
  }
}
