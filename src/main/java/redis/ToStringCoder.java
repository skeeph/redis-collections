package redis;

import java.util.concurrent.atomic.AtomicInteger;

public class ToStringCoder<K, V> implements Coder<K, V> {
  private final String pattern;

  public ToStringCoder(String pattern) {
    this.pattern = pattern;
  }

  @Override
  public K loadKey(String key) {
    return (K) loadObject(key);

  }

  @Override
  public V loadValue(String serializedValue) {
    return (V) loadObject(serializedValue);
  }

  @Override
  public String encodeKey(Object key) {
    if (key == null) {
      return pattern + "null";
    } else {
      return pattern + key.toString().replaceAll(pattern, "") + "/" + key.getClass().getName();

    }
  }

  @Override
  public String encodeValue(V value) {
    if (value == null) return "null";
    return value.toString() + "/" + value.getClass().getName();
  }

  private Object loadObject(String serialized) {
    if (serialized == null) return null;
    serialized = serialized.replace(pattern, "");
    if ("null".equals(serialized)) return null;
    int delimiter = serialized.lastIndexOf("/");
    String content = serialized.substring(0, delimiter);
    if ("null".equals(content)) return null;
    String type = serialized.substring(delimiter + 1);
    try {
      Class<?> aClass = Class.forName(type);
      if (aClass.equals(Integer.class)) return Integer.valueOf(content);
      else if (aClass.equals(String.class)) return content;
      else if (aClass.equals(Character.class)) return content.charAt(0);
      else if (aClass.equals(Boolean.class)) return Boolean.valueOf(content);
      else if (aClass.equals(IntegerEnum.class)) return IntegerEnum.valueOf(content);
      else if (aClass.equals(Long.class)) return Long.valueOf(content);
      else if (aClass.equals(AtomicInteger.class)) return new AtomicInteger(Integer.parseInt(content));
      else throw new RuntimeException("Unsupported type: " + type);
    } catch (ClassNotFoundException e) {
      throw new RuntimeException(e);
    }
  }

}
