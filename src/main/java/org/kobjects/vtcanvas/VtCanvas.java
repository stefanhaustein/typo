package org.kobjects.vtcanvas;


public class VtCanvas {

  static int to256(int rgb) {
    int r = (rgb >> 16) & 255;
    int g = (rgb >> 8) & 255;
    int b = rgb & 255;

    if (r == g && g == b) {
      return 232 + (int) (r / 10.666);
    }
    r = (int) (r/42.666);
    g = (int) (g/42.666);
    b = (int) (b/42.666);

    return 16 + 36*r + 6*g + b;
  }

  int width;
  int height;

  int[] rgb;

  public VtCanvas(int width, int height) {
    this.width = width;
    this.height = height;
    rgb = new int[width * height];
  }

  int getPixel(int x, int y) {
    return rgb[y * width + x];
  }

  void setPixel(int x, int y, int color) {
    rgb[y * width + x] = color;
  }

  public String toString() {
    int pos = 0;
    StringBuilder sb = new StringBuilder();
    for (int y = 0; y < height; y += 2) {
      int lastFg = -1;
      int lastBg = -1;
      for (int x = 0; x < width; x++) {
        int fg = rgb[pos];
        int bg = rgb[pos + width];
        if (lastFg != fg) {
          sb.append("\u001b[38;2;");
          sb.append((fg >> 16) & 255).append(";");
          sb.append((fg >> 8) & 255).append(";");
          sb.append(fg & 255).append("m");
          lastFg = fg;
        }
        if (lastBg != bg){
          sb.append("\u001b[48;2;");
          sb.append((bg >> 16) & 255).append(";");
          sb.append((bg >> 8) & 255).append(";");
          sb.append(bg & 255).append("m");
          lastBg = bg;
        }
        // int fg8 = to256(rgb[pos]);
        // int bg8 = to256(rgb[pos + width]);
        //  sb.append("\u001b[38;5;").append(fg).append("m"); // fg
        //        sb.append("\u001b[48;5;").append(bg).append("m");  // bg
        sb.append("\u2580"); // Upper half block;
        pos++;
      }
      pos += width;
      sb.append("\u001B[0m\n");
    }
    return sb.toString();
  }
}
