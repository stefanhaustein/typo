package org.kobjects.typo.io;


public class ImageData {
  public final int width;
  public final int height;
  public final byte[] data;

  public ImageData(int width, int height) {
    this.width = width;
    this.height = height;
    this.data = new byte[width * height * 4];
  }

  public String dump() {
    StringBuilder sb = new StringBuilder();
    BlockChar blockChar = new BlockChar();

    for (int y = 0; y < height - 7; y += 8) {
      int pos = y * width * 4;
      String lastFg = "";
      String lastBg = "";
      for (int x = 0; x < width - 3; x += 4) {
        blockChar.load(data, pos, width * 4);
        String fg = Ansi.fgColor(blockChar.fgColor[0], blockChar.fgColor[1], blockChar.fgColor[2]);
        String bg = Ansi.bgColor(blockChar.bgColor[0], blockChar.bgColor[1], blockChar.bgColor[2]);
        if (!fg.equals(lastFg)) {
          sb.append(fg);
          lastFg = fg;
        }
        if (!bg.equals(lastBg)) {
          sb.append(bg);
          lastBg = bg;
        }
        sb.append(blockChar.character);
        pos += 16;
      }
      sb.append(Ansi.RESET).append("\n");
    }
    return sb.toString();
  }


}
