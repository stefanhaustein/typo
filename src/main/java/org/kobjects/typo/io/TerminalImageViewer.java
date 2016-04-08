package org.kobjects.typo.io;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;

import javax.imageio.ImageIO;

public class TerminalImageViewer {



  public static void main(String[] args) throws IOException {
    if (args.length == 0) {
      System.out.println("Image file name required. Use -w to set the width in characters (default: 80).");
      return;
    }

    int w = 80 * 4;

    for (int i = 0; i < args.length; i++) {
      String name = args[i];
      if (name.equals("-w")) {
        w = 4 * Integer.parseInt(args[++i]);
        continue;
      }

      BufferedImage original;
      if (name.startsWith("http://") || name.startsWith("https://")) {
        URL url = new URL(name);
        original = ImageIO.read(url);
      } else {
        original = ImageIO.read(new File(args[0]));
      }

      int ow = original.getWidth();
      int oh = original.getHeight();
      int h = oh * w / ow;

      BufferedImage image = original;
      if (w != ow) {
        image = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics = image.createGraphics();
        graphics.drawImage(original, 0, 0, w, h, null);
      }

      ImageData imageData = new ImageData(w, h);
      byte[] data = imageData.data;
      int[] rgbArray = new int[w];
      for (int y = 0; y < image.getHeight(); y++) {
        image.getRGB(0, y, image.getWidth(), 1, rgbArray, 0, w);
        int pos = y * w * 4;
        for (int x = 0; x < w; x++) {
          int rgb = rgbArray[x];
          data[pos++] = (byte) (rgb >> 16);
          data[pos++] = (byte) (rgb >> 8);
          data[pos++] = (byte) rgb;
          pos++;
        }
      }
      System.out.println(imageData.dump());
    }
  }
}
