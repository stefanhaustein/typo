package org.kobjects.typo.io;

import java.util.Arrays;

class BlockChar {
  static int[] MAP = new int[]{
      0x00000000, ' ',

      0x000ff000, '\u2501',
      0x000cc000, '\u2578',
      0x00033000, '\u257a',

      0xffff0000, '\u2580',  // upper 1/2

      0x0000000f, '\u2581',  // lower 1/8
      0x000000ff, '\u2582',  // lower 1/4
      0x00000fff, '\u2583',
      0x0000ffff, '\u2584',  // lower 1/2
      0x000fffff, '\u2585',
      0x00ffffff, '\u2586',  // lower 3/4
      0x0fffffff, '\u2587',
      0xffffffff, '\u2588',  // full

      0xeeeeeeee, '\u258a',  // left 3/4
      0xcccccccc, '\u258c',  // left 1/2
      0x88888888, '\u258e',  // left 1/4

//      0xf0000000, '\u2595',

      0x0000cccc, '\u2596',  // quadrant lower left
      0x00003333, '\u2597',  // quadrant lower right
      0xcccc0000, '\u2598',  // quadrant upper left
      0xccccffff, '\u2599',
      0xcccc3333, '\u259a',  // Q UL/LR
      0xffffcccc, '\u259b',
      0xffff3333, '\u259c',
      0x33330000, '\u259d',
      0x3333cccc, '\u259e',
      0x3333ffff, '\u259f',


      0x000137f0, '\u25e2',
      0x0008cef0, '\u25e3',
      0x000fec80, '\u25e4',
      0x000f7310, '\u25e5'
/*
      0x0033ff00, '\u25e2',
      0x00bbff00, '\u25e3',
      0x00ff3300, '\u25e4',
      0x00ffbb00, '\u25e5' */
  };

  int[] min = new int[3];
  int[] max = new int[3];
  int[] bgColor = new int[3];
  int[] fgColor = new int[3];

  char character;

  void load(byte[] data, int p0, int scanWidth) {
    Arrays.fill(min, 255);
    Arrays.fill(max, 0);
    Arrays.fill(bgColor, 0);
    Arrays.fill(fgColor, 0);

    int pos = p0;
    for (int y = 0; y < 8; y++) {
      for (int x = 0; x < 4; x++) {
        for (int i = 0; i < 3; i++) {
          int d = data[pos++] & 255;
          min[i] = Math.min(min[i], d);
          max[i] = Math.max(max[i], d);
        }
        pos++;  // Alpha
      }
      pos += scanWidth - 16;
    }

    int splitIndex = 0;
    int bestSplit = 0;
    for (int i = 0; i < 3; i++) {
      if (max[i] - min[i] > bestSplit) {
        bestSplit = max[i] - min[i];
        splitIndex = i;
      }
    }
    int splitValue = min[splitIndex] + bestSplit / 2;

    int bits = 0;
    int fgCount = 0;
    int bgCount = 0;

    pos = p0;
    for (int y = 0; y < 8; y++) {
      for (int x = 0; x < 4; x++) {
        bits = bits << 1;
        int[] avg;
        if ((data[pos + splitIndex] & 255) > splitValue) {
          avg = fgColor;
          bits |= 1;
          fgCount++;
        } else {
          avg = bgColor;
          bgCount++;
        }
        for (int i = 0; i < 3; i++) {
          avg[i] += data[pos++] & 255;
        }
        pos++;  // Alpha
      }
      pos += scanWidth - 16;
    }

    for (int i = 0; i < 3; i++) {
      if (bgCount != 0) {
        bgColor[i] /= bgCount;
      }
      if (fgCount != 0) {
        fgColor[i] /= fgCount;
      }
    }

    int inverse = (~bits) & 0xffffffff;
    int bestDiff = Integer.MAX_VALUE;
    boolean invert = false;
    for (int i = 0; i < MAP.length; i += 2) {
      int diff = Integer.bitCount(MAP[i] ^ bits);
      if (diff < bestDiff) {
        character = (char) MAP[i + 1];
        bestDiff = diff;
        invert = false;
      }
      diff = Integer.bitCount(MAP[i] ^ inverse);
      if (diff < bestDiff) {
        character = (char) MAP[i + 1];
        bestDiff = diff;
        invert = true;
      }
    }
/*
    if (bestDiff > 8) {
      invert = false;
      character = " \u2591\u2592\u2593\u2588".charAt(Math.min(4, fgCount * 5 / 32));
    }*/

    if (invert) {
      int[] tmp = bgColor;
      bgColor = fgColor;
      fgColor = tmp;
    }
  }
}
