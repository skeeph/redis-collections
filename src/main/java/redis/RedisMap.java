package redis;

import redis.clients.jedis.Jedis;

import java.util.AbstractMap;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

@SuppressWarnings("WeakerAccess")
public class RedisMap<K, V> implements Map<K, V> {
  Jedis jedis;
  String pattern;
  Coder<K,V> coder;

  public RedisMap() {
    //TODO: Добавить настройки
    jedis = new Jedis();
    pattern = Long.toString(System.currentTimeMillis());
    coder = new ToStringCoder<>(pattern);
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
    return !jedis.keys(coder.encodeKey(key)).isEmpty();
  }

  @Override
  public boolean containsValue(Object value) {
    //TODO: Придумать более быстрый вариант
    Set<String> keys = jedis.keys(pattern + "*");
    for (String key : keys) {
      V integer = get(coder.loadKey(key));
      if (value == null && null == integer) return true;
      if (value != null && value.equals(integer)) return true;
    }
    return false;
  }

  @Override
  public V get(Object key) {
    String s = jedis.get(coder.encodeKey(key));
    return coder.loadValue(s);
  }

  @Override
  public V put(K key, V value) {
    String s = jedis.get(coder.encodeKey(key));
    V oldValue = coder.loadValue(s);
    jedis.set(coder.encodeKey(key), coder.encodeValue(value));
    return oldValue;
  }

  @Override
  public V remove(Object key) {
    V val = get(key);
    jedis.del(coder.encodeKey(key));
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
    return jedis.keys(pattern + "*").stream().map(coder::loadKey).collect(Collectors.toSet());
  }

  @Override
  public Collection<V> values() {
    Set<V> data = new HashSet<>();
    for (String key : jedis.keys(pattern + "*")) {
      data.add(get(coder.loadKey(key)));
    }
    return data;
  }

  @Override
  public Set<Entry<K, V>> entrySet() {
    //TODO: Придумать вариант умнее
    Set<Map.Entry<K, V>> entrySet = new HashSet<>();
    for (String key : jedis.keys(pattern + "*")) {
      entrySet.add(new AbstractMap.SimpleEntry<>(coder.loadKey(key), get(coder.loadKey(key))));
    }
    return entrySet;
  }

  @Override
  public V getOrDefault(Object key, V defaultValue) {
    V saved = get(key);
    if (saved == null && !containsKey(key)) saved = defaultValue;
    return saved;
  }

  @Override
  public boolean remove(Object key, Object value) {
    if (containsKey(key)) {
      V integer = get(key);
      if ((integer == value || value.equals(integer))) {
        remove(key);
        return true;
      }
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
    if (containsKey(key)) {
      V integer = get(key);
      put(key, value);
      return integer;
    }
    return null;
  }

  @Override
  public boolean replace(K key, V oldValue, V newValue) {
    V integer = get(key);
    if (oldValue == integer || oldValue.equals(integer)) {
      put(key, newValue);
      return true;
    }
    return false;
  }

  @Override
  public void replaceAll(BiFunction<? super K, ? super V, ? extends V> function) {
    for (Entry<K, V> e : entrySet()) {
      K key = e.getKey();
      V value = e.getValue();

      V newValue = function.apply(key, value);
      put(key, newValue);
    }
  }
}
