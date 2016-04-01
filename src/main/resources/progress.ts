class Vector {
    constructor(public x: number,
                public y: number,
                public z: number) {
    }
    static times(k: number, v: Vector): Vector { return new Vector(k * v.x, k * v.y, k * v.z); }
    static minus(v1: Vector, v2: Vector): Vector { return new Vector(v1.x - v2.x, v1.y - v2.y, v1.z - v2.z); }
    static plus(v1: Vector, v2: Vector): Vector { return new Vector(v1.x + v2.x, v1.y + v2.y, v1.z + v2.z); }
    static dot(v1: Vector, v2: Vector): number { return v1.x * v2.x + v1.y * v2.y + v1.z * v2.z; }
    static mag(v: Vector): number { return Math.sqrt(v.x * v.x + v.y * v.y + v.z * v.z); }
    static norm(v: Vector): Vector {
        var mag = Vector.mag(v);
        var div = (mag === 0) ? Infinity : 1.0 / mag;
        return Vector.times(div, v);
    }
    static cross(v1: Vector, v2: Vector): Vector {
        return new Vector(v1.y * v2.z - v1.z * v2.y,
                          v1.z * v2.x - v1.x * v2.z,
                          v1.x * v2.y - v1.y * v2.x);
    }
}

class Color {
    constructor(public r: number,
                public g: number,
                public b: number) {
    }
    static scale(k: number, v: Color): Color { return new Color(k * v.r, k * v.g, k * v.b); }
    static plus(v1: Color, v2: Color): Color { return new Color(v1.r + v2.r, v1.g + v2.g, v1.b + v2.b); }
    static times(v1: Color, v2: Color): Color { return new Color(v1.r * v2.r, v1.g * v2.g, v1.b * v2.b); }
    static white: Color = new Color(1.0, 1.0, 1.0);
    static grey: Color = new Color(0.5, 0.5, 0.5);
    static black: Color = new Color(0.0, 0.0, 0.0);
    static background: Color = Color.black;
    static defaultColor: Color = Color.black;
    static toDrawingColor(c: Color): Color {
        var legalize = function(d:number):number {return d > 1 ? 1 : d;}
        return new Color(
            Math.floor(legalize(c.r) * 255),
            Math.floor(legalize(c.g) * 255),
            Math.floor(legalize(c.b) * 255))
        }
}


interface Ray {
    start: Vector;
    dir: Vector;
}

interface Intersection {
    thing: Thing;
    ray: Ray;
    dist: number;
}

interface Surface {
    diffuse: (pos: Vector) => Color;
    specular: (pos: Vector) => Color;
    reflect: (pos: Vector) => number;
    roughness: number;
}

interface Thing {
    intersect: (ray: Ray) => Intersection;
    normal: (pos: Vector) => Vector;
    surface: Surface;
}

interface Light {
    pos: Vector;
    color: Color;
}
