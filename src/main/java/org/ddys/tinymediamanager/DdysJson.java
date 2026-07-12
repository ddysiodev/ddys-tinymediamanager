package org.ddys.tinymediamanager;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

final class DdysJson {
  private final String input;
  private int          pos;

  private DdysJson(String input) {
    String text = input == null ? "" : input;
    this.input = text.startsWith("\uFEFF") ? text.substring(1) : text;
  }

  static Object parse(String input) {
    DdysJson parser = new DdysJson(input);
    Object value = parser.readValue();
    parser.skipWhitespace();
    if (parser.pos != parser.input.length()) {
      throw new IllegalArgumentException("Trailing JSON content");
    }
    return value;
  }

  private Object readValue() {
    skipWhitespace();
    if (pos >= input.length()) {
      throw new IllegalArgumentException("Empty JSON");
    }
    char ch = input.charAt(pos);
    return switch (ch) {
      case '{' -> readObject();
      case '[' -> readArray();
      case '"' -> readString();
      case 't' -> readLiteral("true", Boolean.TRUE);
      case 'f' -> readLiteral("false", Boolean.FALSE);
      case 'n' -> readLiteral("null", null);
      default -> readNumber();
    };
  }

  private Map<String, Object> readObject() {
    expect('{');
    Map<String, Object> map = new LinkedHashMap<>();
    skipWhitespace();
    if (peek('}')) {
      pos++;
      return map;
    }
    while (true) {
      skipWhitespace();
      String key = readString();
      skipWhitespace();
      expect(':');
      map.put(key, readValue());
      skipWhitespace();
      if (peek('}')) {
        pos++;
        return map;
      }
      expect(',');
    }
  }

  private List<Object> readArray() {
    expect('[');
    List<Object> list = new ArrayList<>();
    skipWhitespace();
    if (peek(']')) {
      pos++;
      return list;
    }
    while (true) {
      list.add(readValue());
      skipWhitespace();
      if (peek(']')) {
        pos++;
        return list;
      }
      expect(',');
    }
  }

  private String readString() {
    expect('"');
    StringBuilder out = new StringBuilder();
    while (pos < input.length()) {
      char ch = input.charAt(pos++);
      if (ch == '"') {
        return out.toString();
      }
      if (ch != '\\') {
        out.append(ch);
        continue;
      }
      if (pos >= input.length()) {
        throw new IllegalArgumentException("Bad JSON escape");
      }
      char esc = input.charAt(pos++);
      switch (esc) {
        case '"' -> out.append('"');
        case '\\' -> out.append('\\');
        case '/' -> out.append('/');
        case 'b' -> out.append('\b');
        case 'f' -> out.append('\f');
        case 'n' -> out.append('\n');
        case 'r' -> out.append('\r');
        case 't' -> out.append('\t');
        case 'u' -> out.append(readUnicode());
        default -> throw new IllegalArgumentException("Bad JSON escape");
      }
    }
    throw new IllegalArgumentException("Unterminated JSON string");
  }

  private char readUnicode() {
    if (pos + 4 > input.length()) {
      throw new IllegalArgumentException("Bad unicode escape");
    }
    int value = Integer.parseInt(input.substring(pos, pos + 4), 16);
    pos += 4;
    return (char) value;
  }

  private Object readNumber() {
    int start = pos;
    if (peek('-')) {
      pos++;
    }
    while (pos < input.length() && Character.isDigit(input.charAt(pos))) {
      pos++;
    }
    boolean decimal = false;
    if (peek('.')) {
      decimal = true;
      pos++;
      while (pos < input.length() && Character.isDigit(input.charAt(pos))) {
        pos++;
      }
    }
    if (pos < input.length() && (input.charAt(pos) == 'e' || input.charAt(pos) == 'E')) {
      decimal = true;
      pos++;
      if (pos < input.length() && (input.charAt(pos) == '+' || input.charAt(pos) == '-')) {
        pos++;
      }
      while (pos < input.length() && Character.isDigit(input.charAt(pos))) {
        pos++;
      }
    }
    if (start == pos) {
      throw new IllegalArgumentException("Expected JSON value");
    }
    String text = input.substring(start, pos);
    return decimal ? Double.parseDouble(text) : Long.parseLong(text);
  }

  private Object readLiteral(String literal, Object value) {
    if (!input.startsWith(literal, pos)) {
      throw new IllegalArgumentException("Invalid JSON literal");
    }
    pos += literal.length();
    return value;
  }

  private void expect(char expected) {
    skipWhitespace();
    if (pos >= input.length() || input.charAt(pos) != expected) {
      throw new IllegalArgumentException("Expected '" + expected + "'");
    }
    pos++;
  }

  private boolean peek(char expected) {
    return pos < input.length() && input.charAt(pos) == expected;
  }

  private void skipWhitespace() {
    while (pos < input.length() && Character.isWhitespace(input.charAt(pos))) {
      pos++;
    }
  }
}
