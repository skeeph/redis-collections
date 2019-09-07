package redis;

public interface Coder<K, V> {
  K loadKey(String key);

  V loadValue(String serializedValue);

  String encodeKey(Object key);

  String encodeValue(V value);
}
