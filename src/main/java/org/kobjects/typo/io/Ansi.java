package org.kobjects.typo.io;

public class Ansi {
  public static String fgColor(byte r, byte g, byte b) {
    return "\u001b[38;2;" + (r & 255) + ";" + (g & 255) + ";" + (b & 255) + "m";
  }

  public static String bgColor(byte r, byte g, byte b) {
    return "\u001b[48;2;" + (r & 255) + ";" + (g & 255) + ";" + (b & 255) + "m";
  }

}
