package randoop.util;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.plumelib.bcelutil.JvmUtil;
import plume.UtilMDE;
import randoop.Globals;

/** Helpers for assertions, and stuff... */
public final class Util {

  private Util() {
    throw new IllegalStateException("no instance");
  }

  public static final String newLine = System.getProperty("line.separator");

  public static boolean iff(boolean a, boolean b) {
    return a == b;
  }

  public static boolean implies(boolean a, boolean b) {
    return !a || b;
  }

  /**
   * If both parameters are null, returns true. If one parameter is null and the other isn't,
   * returns false. Otherwise, returns o1.equals(o2).
   *
   * @param o1 first object to test
   * @param o2 second object to test
   * @return true if arguments are both null or equal, and false otherwise
   */
  public static boolean equalsWithNull(Object o1, Object o2) {
    if (o1 == null) {
      return o2 == null;
    }
    return o2 != null && (o1.equals(o2));
  }

  public static boolean isJavaIdentifier(String s) {
    if (s == null || s.length() == 0 || !Character.isJavaIdentifierStart(s.charAt(0))) {
      return false;
    }
    for (int i = 1; i < s.length(); i++) {
      if (!Character.isJavaIdentifierPart(s.charAt(i))) {
        return false;
      }
    }
    return true;
  }

  public static String convertToHexString(String unicodeString) {
    char[] chars = unicodeString.toCharArray();
    StringBuilder output = new StringBuilder();
    for (int i = 0; i < chars.length; i++) {
      output.append("\\u");
      String hex = Integer.toHexString(chars[i]);
      if (hex.length() < 4) output.append("0");
      if (hex.length() < 3) output.append("0");
      if (hex.length() < 2) output.append("0");
      if (hex.length() < 1) output.append("0");

      output.append(hex);
    }
    return output.toString();
  }

  public static int occurCount(StringBuilder text, String pattern) {
    if (pattern.length() == 0) throw new IllegalArgumentException("empty pattern");
    int i = 0;
    int currIdx = text.indexOf(pattern);
    while (currIdx != -1) {
      i++;
      currIdx = text.indexOf(pattern, currIdx + 1);
    }
    return i;
  }

  /**
   * Format a hanging paragraph: The first line starts at the margin, and every subsequent line
   * starts indented by {@code indentWidth}. Each line is no more than {@code colWidth} characters
   * long.
   *
   * @param string the paragraph to format
   * @param colWidth the full column width
   * @param indentWidth the number of spaces before each line other than the first line
   * @return a string representation of the formatted paragraph, include line separators
   */
  public static String hangingParagraph(String string, int colWidth, int indentWidth) {
    if (string == null) throw new IllegalArgumentException("string cannot be null.");
    if (indentWidth > colWidth) {
      throw new IllegalArgumentException("indentWidth cannot be greater than columnWidth");
    }

    String indentString = new String(new char[indentWidth]).replace("\0", " ");

    StringBuilder b = new StringBuilder();

    boolean firstLine = true;
    while (true) {
      // Determine line's length (exclusive of intent if any)
      int lineLength = firstLine ? colWidth : colWidth - indentWidth;

      if (lineLength > string.length()) {
        if (!firstLine) {
          b.append(indentString);
        }
        b.append(string);
        b.append(Globals.lineSep);
        return b.toString();
      }

      // i is index of whitespace
      int i = lineLength;
      while (i > 0) {
        if (Character.isWhitespace(string.charAt(i))) {
          break;
        }
        i--;
      }
      if (i == 0) {
        i = lineLength;
      }

      // Add indent.
      if (!firstLine) {
        b.append(indentString);
      }
      b.append(string.substring(0, i));
      b.append(Globals.lineSep);
      string = string.substring(i + 1);
      firstLine = false;
    }
  }

  public static String createArgListJVML(Class<?>[] paramClasses) {
    StringBuilder b = new StringBuilder();
    b.append("(");
    for (int i = 0; i < paramClasses.length; i++) {
      Class<?> cls = paramClasses[i];

      // If primitive, Class.getName() returns the keyword. Convert to JVML.
      if (cls.isPrimitive()) {
        b.append(JvmUtil.primitiveTypeNameToFieldDescriptor(cls.getName()));
        continue;
      }

      boolean isArray = cls.isArray();

      // If primitive array, Class.getName() gives the JML representation.
      if (isArray && cls.isPrimitive()) {
        b.append(cls.getName());
        continue;
      }

      // If object array, Class.getName() returns almost the JVML
      // representation,
      // except for the element class, which uses "." instead of "/" to separate
      // package names.
      if (isArray) {
        b.append(cls.getName().replace('.', '/'));
        continue;
      }

      // Is object, non-array. Class.getName() returns foo.bar.Baz. Convert to
      // JVML.
      b.append(JvmUtil.binaryNameToFieldDescriptor(paramClasses[i].getName()));
    }
    b.append(")");
    return b.toString();
  }

  /**
   * Replace occurrences of words from this map with corresponding replacements. Only performs
   * replacements of full words (using regular expression word delimiters).
   *
   * @param text the text to search for occurrences of names in this map
   * @param replacements the map of replacements to perform
   * @return the text modified by replacing original names with replacement names
   */
  public static String replaceWords(String text, Map<String, String> replacements) {
    Pattern namesPattern =
        Pattern.compile("\\b(" + UtilMDE.join(replacements.keySet().toArray(), "|") + ")\\b");
    Matcher namesMatcher = namesPattern.matcher(text);
    StringBuilder b = new StringBuilder();
    int position = 0;
    while (namesMatcher.find(position)) {
      b.append(text.substring(position, namesMatcher.start(1)));
      b.append(replacements.get(namesMatcher.group(1)));
      position = namesMatcher.end(1);
    }
    b.append(text.substring(position));
    return b.toString();
  }
}
