package org.kobjects.vtcanvas;


public class Test {
  public static void main(String[] args) {
    int nx = 80;
    int ny = 40;
    VtCanvas canvas = new VtCanvas(nx, ny);
    for (int j = ny - 1; j >= 0; j--) {
      for (int i = 0; i < nx; i++) {
        float r = i / (float) nx;
        float g = j / (float) ny;
        float b = 0.2f;
        int ir = (int) (255.99f * r);
        int ig = (int) (255.99f * g);
        int ib = (int) (255.99f * b);
        int rgb = (ir << 16) | (ig << 8) | ib;
        canvas.setPixel(i, j, rgb);
      }
    }
    System.out.print(canvas.toString());
  }
}
