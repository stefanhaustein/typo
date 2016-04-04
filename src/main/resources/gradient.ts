let d = new ImageData(40, 20);
let pos = 0;
for (let y = 0; y < d.height; y++) {
  for (let x = 0; x < d.width; x++) {
    d.data[pos++] = 255 * x / d.width;
    d.data[pos++] = 255 * y / d.height;
    d.data[pos++] = 50;
    pos++;
    console.log("x: " + x + " y: " + y + " pos: " + pos);
  }
}
console.log(d);
