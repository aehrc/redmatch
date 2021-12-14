package au.csiro.redmatch.util;

import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DocumentUtils {

  static final Pattern newLine = Pattern.compile("\\R");
  public static String calculateSnippet(String text, Range range) {
    Position start = range.getStart();
    Position end = range.getEnd();
    return text.substring(getPosition(start, text), getPosition(end, text));
  }

  public static int getPosition(Position pos, String text) {
    int lineNum = pos.getLine();
    if (lineNum == 0) {
      return pos.getCharacter();
    }

    Matcher matcher = newLine.matcher(text);
    for(int i = 0; i < lineNum; i++) {
      if (!matcher.find()) {
        throw new RuntimeException("Invalid text and position: " + text + ", " + pos);
      }
    }
    return matcher.end() + pos.getCharacter();
  }
}
