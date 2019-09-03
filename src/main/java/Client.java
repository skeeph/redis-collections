import redis.RedisMap;

import java.util.Map;

public class Client {
  public static void main(String[] args) {
    Map<String, Integer> redis = new RedisMap<>();
    redis.put("Hello", 1);
    redis.put("World", 21);
    System.out.println(redis.get("Hello"));;
    System.out.println(redis.containsValue(21));
  }
}
