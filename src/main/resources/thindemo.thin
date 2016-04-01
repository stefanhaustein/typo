class Link {
  value: int;
  next: Link;
}

class List {
  first: Link;
  last: Link;

  append(value: int): void {
    var link = new Link();
    link.value = value;

    if (this.first == null) this.first = link;
    else this.last.next = link;
    this.last = link;
  }
}

var list = new List();
list.append(1);
list.append(2);
list.append(3);

var total = 0.0;
var link = list.first;
while (link != null) {
  total = total + link.value;
  link = link.next;
}

console.log("Total: " + total);
