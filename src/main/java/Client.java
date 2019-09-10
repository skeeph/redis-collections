import redis.RedisList;
import redis.RedisMap;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class Client {
  public static void main(String[] args) {
    List<String> list = new RedisList<>();
    list.clear();
    list.add("A");
    list.add("L");
    list.add("C");
    list.add("D");
    list.add("F");
    list.add("A");

    list.set(0,"R");

    System.out.println(Arrays.toString(list.toArray(new String[0])));
  }
}
