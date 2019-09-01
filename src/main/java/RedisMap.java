import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisCluster;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;

@SuppressWarnings("WeakerAccess")
public class RedisMap<K, V> implements Map<String, Integer> {
  Jedis jedis;
  String pattern;

  public RedisMap() {
    //TODO: Добавить настройки
    jedis = new Jedis();
    pattern = Long.toString(System.currentTimeMillis());
  }

  @Override
  public int size() {
    return jedis.keys(pattern + "*").size();
  }

  @Override
  public boolean isEmpty() {
    return jedis.keys(pattern + "*").isEmpty();
  }

  @Override
  public boolean containsKey(Object key) {
    return !jedis.keys(getKey(key)).isEmpty();
  }

  @Override
  public boolean containsValue(Object value) {
    //TODO: Придумать более быстрый вариант
    Set<String> keys = jedis.keys(pattern + "*");
    for (String key : keys) {
      if (value.equals(Integer.valueOf(jedis.get(key)))) return true;
    }
    return false;
  }

  @Override
  public Integer get(Object key) {
    String s = jedis.get(getKey(key));
    Integer x = null;
    if (s != null) {
      x = Integer.valueOf(s);
    }
    return x;
  }

  @Override
  public Integer put(String key, Integer value) {
    jedis.set(getKey(key), Integer.toString(value));
    return value;
  }

  @Override
  public Integer remove(Object key) {
    Integer val = get(key);
    jedis.del(getKey(key));
    return val;
  }

  @Override
  public void putAll(Map<? extends String, ? extends Integer> m) {
    for (Entry<? extends String, ? extends Integer> e : m.entrySet()) {
      put(e.getKey(), e.getValue());
    }
  }

  @Override
  public void clear() {

  }

  @Override
  public Set<String> keySet() {
    return jedis.keys(pattern + "*");
  }

  @Override
  public Collection<Integer> values() {
    Map<String, Integer> data = new HashMap<>();
    for (String key : jedis.keys(pattern + "*")) {
      data.put(key, get(key));
    }
    return data.values();
  }

  @Override
  public Set<Entry<String, Integer>> entrySet() {
    //TODO: Придумать вариант умнее
    Map<String, Integer> data = new HashMap<>();
    for (String key : jedis.keys(pattern + "*")) {
      data.put(key, get(key));
    }
    return data.entrySet();
  }

  @Override
  public Integer getOrDefault(Object key, Integer defaultValue) {
    Integer integer = get(key);
    if (integer == null) integer = defaultValue;
    return integer;
  }

  @Override
  public boolean remove(Object key, Object value) {
    Integer integer = get(key);
    if (value.equals(integer)) {
      remove(key);
      return true;
    }
    return false;
  }

  @Override
  public void forEach(BiConsumer<? super String, ? super Integer> action) {
    for (Entry<String, Integer> entry : entrySet()) {
      action.accept(entry.getKey(), entry.getValue());
    }
  }

  @Override
  public Integer replace(String key, Integer value) {
    Integer integer = get(key);
    if (integer != null) {
      put(key, value);
      return integer;
    }
    return null;
  }

  @Override
  public boolean replace(String key, Integer oldValue, Integer newValue) {
    Integer integer = get(key);
    if (oldValue.equals(integer)) {
      put(key, newValue);
      return true;
    }
    return false;
  }

  private String getKey(Object key) {
    return pattern + key;
  }
}
