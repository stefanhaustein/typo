package org.kobjects.typo.type;

import org.kobjects.typo.parser.ParsingContext;

import java.util.ArrayList;

public class UnionType implements Type {
  private final Type[] members;

  public UnionType(Type... members) {
    ArrayList<Type> list = new ArrayList<Type>();
    for (Type m: members) {
      if (m instanceof UnionType) {
        UnionType u = (UnionType) m;
        for (Type inner: u.members) {
          list.add(inner);
        }
      } else {
        list.add(m);
      }
    }
    this.members = list.toArray(new Type[list.size()]);
  }

  @Override
  public String name() {
    StringBuilder sb = new StringBuilder();
    if (members.length > 0) {
      sb.append(members[0].name());
      for (int i = 1; i < members.length; i++) {
        sb.append('|');
        sb.append(members[i].name());
      }
    }
    return sb.toString();
  }

  @Override
  public Type resolve(ParsingContext context) {
    for (int i = 0; i < members.length; i++) {
      members[i] = members[i].resolve(context);
    }
    return this;
  }

  @Override
  public boolean assignableFrom(Type type) {
    for (Type member: members) {
      if (member.assignableFrom(type)) {
        return true;
      }
    }
    return false;
  }
}
