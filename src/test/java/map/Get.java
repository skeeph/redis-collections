package map;

import redis.RedisMap;

import java.util.HashMap;
import java.util.Hashtable;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.WeakHashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ConcurrentSkipListMap;

public class Get {
  private static void realMain(String[] args) throws Throwable {
    testMap(new Hashtable<>());
    testMap(new HashMap<>());
    testMap(new IdentityHashMap<>());
    testMap(new LinkedHashMap<>());
    testMap(new ConcurrentHashMap<>());
    testMap(new WeakHashMap<>());
    testMap(new TreeMap<>());
    testMap(new ConcurrentSkipListMap<>());
    testMap(new RedisMap<>());
  }

  private static void put(Map<Character,Boolean> m,
                          Character key, Boolean value,
                          Boolean oldValue) {
    if (oldValue != null) {
      check("containsValue(oldValue)", m.containsValue(oldValue));
      check("values.contains(oldValue)", m.values().contains(oldValue));
    }
    equal(m.put(key, value), oldValue);
    equal(m.get(key), value);
    check("containsKey", m.containsKey(key));
    check("keySet.contains", m.keySet().contains(key));
    check("containsValue", m.containsValue(value));
    check("values.contains",  m.values().contains(value));
    check("!isEmpty", ! m.isEmpty());
  }

  private static void testMap(Map<Character,Boolean> m) {
    // We verify following assertions in get(Object) method javadocs
    boolean permitsNullKeys = (! (m instanceof ConcurrentMap ||
        m instanceof Hashtable     ||
        m instanceof SortedMap));
    boolean permitsNullValues = (! (m instanceof ConcurrentMap ||
        m instanceof Hashtable));
    boolean usesIdentity = m instanceof IdentityHashMap;

    System.err.println(m.getClass());
    put(m, 'A', true,  null);
    put(m, 'A', false, true);       // Guaranteed identical by JLS
    put(m, 'B', true,  null);
    put(m, new Character('A'), false, usesIdentity ? null : false);
    if (permitsNullKeys) {
      try {
        put(m, null, true,  null);
        put(m, null, false, true);
      }
      catch (Throwable t) { unexpected(m.getClass().getName(), t); }
    } else {
      try { m.get(null); fail(m.getClass().getName() + " did not reject null key"); }
      catch (NullPointerException e) {}
      catch (Throwable t) { unexpected(m.getClass().getName(), t); }

      try { m.put(null, true); fail(m.getClass().getName() + " did not reject null key"); }
      catch (NullPointerException e) {}
      catch (Throwable t) { unexpected(m.getClass().getName(), t); }
    }
    if (permitsNullValues) {
      try {
        put(m, 'C', null, null);
        put(m, 'C', true, null);
        put(m, 'C', null, true);
      }
      catch (Throwable t) { unexpected(m.getClass().getName(), t); }
    } else {
      try { m.put('A', null); fail(m.getClass().getName() + " did not reject null key"); }
      catch (NullPointerException e) {}
      catch (Throwable t) { unexpected(m.getClass().getName(), t); }

      try { m.put('C', null); fail(m.getClass().getName() + " did not reject null key"); }
      catch (NullPointerException e) {}
      catch (Throwable t) { unexpected(m.getClass().getName(), t); }
    }
  }

  //--------------------- Infrastructure ---------------------------
  static volatile int passed = 0, failed = 0;
  static void pass() { passed++; }
  static void fail() { failed++; (new Error("Failure")).printStackTrace(System.err); }
  static void fail(String msg) { failed++; (new Error("Failure: " + msg)).printStackTrace(System.err); }
  static void unexpected(String msg, Throwable t) { System.err.println("Unexpected: " + msg); unexpected(t); }
  static void unexpected(Throwable t) { failed++; t.printStackTrace(System.err); }
  static void check(boolean cond) { if (cond) pass(); else fail(); }
  static void check(String desc, boolean cond) { if (cond) pass(); else fail(desc); }
  static void equal(Object x, Object y) {
    if(Objects.equals(x,y)) pass(); else fail(x + " not equal to " + y);
  }

  public static void main(String[] args) throws Throwable {
    try { realMain(args); } catch (Throwable t) { unexpected(t); }

    System.out.printf("%nPassed = %d, failed = %d%n%n", passed, failed);
    if (failed > 0) throw new Error("Some tests failed");
  }
}
