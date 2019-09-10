import redis.RedisList;
import redis.RedisMap;

import java.util.Arrays;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

public class Client {
  public static void main(String[] args) {
    List<String> list = new RedisList<>(Arrays.asList("A","L","C","A","F","F","A"));
    list.add(3,"X");


    System.out.println(Arrays.toString(list.toArray(new String[0])));
  }
}
