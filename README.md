
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


#### Plan

- Get the raytracer example compiling (with added explicit return types; got it to parse, but compilation fails because reduce is not yet implemented (and there are probably other issues))
- Get the raytracer example running (rendering to VtCanvas)
- Some Cleanup
- WebAssembly output prototype
- Jsdoc output prototype

#### Missing

- Union types and strict nullability
- Dictionaries
- Class member visibility enforcement
- Class visibility enforcement
- Line numbers in later phases
- Const support and enforcement 
- Generics
- Probably a lot more
