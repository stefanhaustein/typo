
#### Goal

Create something that relates to (a strict subset of) TypeScript like Rhino relates to JavaScript

#### Working

- Some trivial demos ([greeters](https://github.com/stefanhaustein/typo/blob/master/src/main/resources/greeter2.ts) and a [linked list example](https://github.com/stefanhaustein/typo/blob/master/src/main/resources/thindemo.thin) converted from [thinscript](https://github.com/evanw/thinscript))

- The [Gradient example](https://github.com/stefanhaustein/typo/blob/master/src/main/resources/gradient.ts)
```
  let d = new ImageData(40, 20);
  let pos = 0;
  for (let y = 0; y < d.height; y++) {
    for (let x = 0; x < d.width; x++) {
      d.data[pos++] = 255 * x / d.width;
      d.data[pos++] = 255 * y / d.height;
      d.data[pos++] = 50;
      pos++;
    }
  }
  console.log(d);
```
![Gradient](http://i.imgur.com/4iC0tS2.png)

- A [strictly typed version of the ts raytracer example](https://github.com/stefanhaustein/typo/blob/master/src/main/resources/raytracer.ts)

  Run it via `load("/raytracer.ts")` from the TS command shell; it will be loaded from the
  Java resources if the project resource folder is set up correctly.

  The output should look decent on the Ubuntu default shell. Unfortunately, the IntelliJ shell
  seems to support 16 colors only.


![Raytrace](http://i.imgur.com/q16umjd.png)




#### Plan

- Some Cleanup (better type checks and interface intersection)
- WebAssembly output prototype
- Jsdoc output prototype

#### Missing

- Strict nullability
- Dictionaries
- Class member visibility enforcement
- Class visibility enforcement
- Const support and enforcement 
- Generics
- Probably a lot more
