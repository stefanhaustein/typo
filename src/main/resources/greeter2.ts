class Greeter {
    constructor(public who: string) { }
    greet(): string {
        return "Hello " + this.who + "!";
    }
};

let greeter = new Greeter("Peter");

console.log(greeter.greet());