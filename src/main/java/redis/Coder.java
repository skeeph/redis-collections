package redis;

public interface Coder<K, V> {
  K loadKey(String key);

  V loadValue(String serializedValue);

  String encodeKey(Object key);

  String encodeValue(V value);

  default String encodeObject(Object value){
    if (value == null) return "null";
    return value.toString() + "_" + value.getClass().getName();
  }
}
