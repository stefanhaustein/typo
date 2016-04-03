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

class Camera {
    public forward: Vector;
    public right: Vector;
    public up: Vector;

    constructor(public pos: Vector, lookAt: Vector) {
        var down = new Vector(0.0, -1.0, 0.0);
        this.forward = Vector.norm(Vector.minus(lookAt, this.pos));
        this.right = Vector.times(1.5, Vector.norm(Vector.cross(this.forward, down)));
        this.up = Vector.times(1.5, Vector.norm(Vector.cross(this.forward, this.right)));
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

interface Scene {
    things: Thing[];
    lights: Light[];
    camera: Camera;
}

class Sphere implements Thing {
    public radius2: number;

    constructor(public center: Vector, radius: number, public surface: Surface) {
        this.radius2 = radius * radius;
    }
    normal(pos: Vector): Vector { return Vector.norm(Vector.minus(pos, this.center)); }
    intersect(ray: Ray): Intersection {
        var eo = Vector.minus(this.center, ray.start);
        var v = Vector.dot(eo, ray.dir);
        var dist = 0;
        if (v >= 0) {
            var disc = this.radius2 - (Vector.dot(eo, eo) - v * v);
            if (disc >= 0) {
                dist = v - Math.sqrt(disc);
            }
        }
        if (dist === 0) {
            return null;
        } else {
            return { thing: this, ray: ray, dist: dist };
        }
    }
}

class Plane implements Thing {
    public normal: (pos: Vector) =>Vector;
    public intersect: (ray: Ray) =>Intersection;
    constructor(norm: Vector, offset: number, public surface: Surface) {
        this.normal = function(pos: Vector): Vector { return norm; }
        this.intersect = function(ray: Ray): Intersection {
            var denom = Vector.dot(norm, ray.dir);
            if (denom > 0) {
                return null;
            } else {
                var dist = (Vector.dot(norm, ray.start) + offset) / (-denom);
                return { thing: this, ray: ray, dist: dist };
            }
        }
    }
}


module Surfaces {
    export var shiny: Surface = {
        diffuse: function(pos: Vector): Color { return Color.white; },
        specular: function(pos: Vector): Color { return Color.grey; },
        reflect: function(pos: Vector): number { return 0.7; },
        roughness: 250
    }
    export var checkerboard: Surface = {
        diffuse: function(pos: Vector): Color {
            if ((Math.floor(pos.z) + Math.floor(pos.x)) % 2 !== 0) {
                return Color.white;
            } else {
                return Color.black;
            }
        },
        specular: function(pos: Vector): Color { return Color.white; },
        reflect: function(pos: Vector): number {
            if ((Math.floor(pos.z) + Math.floor(pos.x)) % 2 !== 0) {
                return 0.1;
            } else {
                return 0.7;
            }
        },
        roughness: 150
    }
}

