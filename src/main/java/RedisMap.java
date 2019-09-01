import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisCluster;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;

@SuppressWarnings("WeakerAccess")
public class RedisMap<K, V> implements Map<K, V> {
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
      V integer = get(loadKey(key));
      if (value == null && null == integer) return true;
      if (value != null && value.equals(integer)) return true;
    }
    return false;
  }

  @Override
  public V get(Object key) {
    String s = jedis.get(getKey(key));
    return loadValue(s);
  }

  @Override
  public V put(K key, V value) {
    String s = jedis.get(getKey(key));
    V oldValue  = loadValue(s);
    jedis.set(getKey(key), toStringValue(value));
    return oldValue;
  }

  private String toStringValue(V value) {
    if (value == null) return "null";
    return value.toString();
  }

  @Override
  public V remove(Object key) {
    V val = get(key);
    jedis.del(getKey(key));
    return val;
  }

  @Override
  public void putAll(Map<? extends K, ? extends V> m) {
    for (Entry<? extends K, ? extends V> e : m.entrySet()) {
      put(e.getKey(), e.getValue());
    }
  }

  @Override
  public void clear() {
    String[] keys = jedis.keys(pattern + "*").toArray(new String[0]);
    jedis.del(keys);
  }

  @Override
  public Set<K> keySet() {
    return jedis.keys(pattern + "*").stream().map(this::loadKey).collect(Collectors.toSet());
  }

  @Override
  public Collection<V> values() {
    Map<String, V> data = new HashMap<>();
    for (String key : jedis.keys(pattern + "*")) {
      data.put(key, get(loadKey(key)));
    }
    return data.values();
  }

  @Override
  public Set<Entry<K, V>> entrySet() {
    //TODO: Придумать вариант умнее
    Map<K, V> data = new HashMap<>();
    for (String key : jedis.keys(pattern + "*")) {
      data.put(loadKey(key), get(key));
    }
    return data.entrySet();
  }

  @Override
  public V getOrDefault(Object key, V defaultValue) {
    V saved = get(key);
    if (saved == null) saved = defaultValue;
    return saved;
  }

  @Override
  public boolean remove(Object key, Object value) {
    V integer = get(key);
    if (value.equals(integer)) {
      remove(key);
      return true;
    }
    return false;
  }

  @Override
  public void forEach(BiConsumer<? super K, ? super V> action) {
    for (Entry<K, V> entry : entrySet()) {
      action.accept(entry.getKey(), entry.getValue());
    }
  }

  @Override
  public V replace(K key, V value) {
    V integer = get(key);
    if (integer != null) {
      put(key, value);
      return integer;
    }
    return null;
  }

  @Override
  public boolean replace(K key, V oldValue, V newValue) {
    V integer = get(key);
    if (oldValue.equals(integer)) {
      put(key, newValue);
      return true;
    }
    return false;
  }

  private String getKey(Object key) {
    if (key == null) {
      key = "null";
    }
    return pattern + key.toString().replaceAll(pattern, "");
  }

  /**
   * Десериализация ключа
   *
   * @param key сериализованный ключ из Redis
   * @return Десериализованный ключ
   */
  private K loadKey(String key) {
    if (key == null) return null;
    key = key.replace(pattern, "");
    if ("null".equals(key)) return null;
    //TODO: murtuzaaliev 01.09.2019 Загружать ключи из строки редиски
    return (K) "TODO";
  }

  /**
   * Десериализация значения
   *
   * @param serializedValue сериализованное значение из Redis
   * @return Десериализованное значение
   */
  private V loadValue(String serializedValue) {
    if (serializedValue == null) return null;
    serializedValue = serializedValue.replace(pattern, "");
    if ("null".equals(serializedValue)) return null;
    //TODO: murtuzaaliev 01.09.2019 Загружать ключи из строки редиски
    return null;
  }
}
