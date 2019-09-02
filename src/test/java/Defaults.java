import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.AbstractMap;
import java.util.AbstractSet;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.WeakHashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.function.BiFunction;
import java.util.function.Supplier;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;

//@SuppressWarnings("ALL")
@SuppressWarnings({"unused", "WeakerAccess", "InfiniteRecursion", "ConstantConditions", "CollectionAddAllCanBeReplacedWithConstructor", "SameParameterValue", "ArraysAsListWithZeroOrOneArgument"})
public class Defaults {

  @Test(dataProvider = "Map<IntegerEnum,String> rw=all keys=withNull values=withNull")
  public void testGetOrDefaultNulls(String description, Map<IntegerEnum, String> map) {
    assertTrue(map.containsKey(null), description + ": null key absent");
    assertNull(map.get(null), description + ": value not null");
    assertEquals(map.get(null), map.getOrDefault(null, EXTRA_VALUE), description + ": values should match");
  }

  @Test(dataProvider = "Map<IntegerEnum,String> rw=all keys=all values=all")
  public void testGetOrDefault(String description, Map<IntegerEnum, String> map) {
    assertTrue(map.containsKey(KEYS[1]), "expected key missing");
    assertEquals(map.get(KEYS[1]), map.getOrDefault(KEYS[1], EXTRA_VALUE), "values should match");
    assertFalse(map.containsKey(EXTRA_KEY), "expected absent key");
    assertEquals(map.getOrDefault(EXTRA_KEY, EXTRA_VALUE), EXTRA_VALUE, "value not returned as default");
    assertNull(map.getOrDefault(EXTRA_KEY, null), "null not returned as default");
  }

  @Test(dataProvider = "Map<IntegerEnum,String> rw=true keys=withNull values=withNull")
  public void testPutIfAbsentNulls(String description, Map<IntegerEnum, String> map) {
    // null -> null
    assertTrue(map.containsKey(null), "null key absent");
    assertNull(map.get(null), "value not null");
    assertNull(map.putIfAbsent(null, EXTRA_VALUE), "previous not null");
    // null -> EXTRA_VALUE
    assertTrue(map.containsKey(null), "null key absent");
    assertEquals(map.get(null), EXTRA_VALUE, "unexpected value");
    assertEquals(map.putIfAbsent(null, null), EXTRA_VALUE, "previous not expected value");
    assertTrue(map.containsKey(null), "null key absent");
    assertEquals(map.get(null), EXTRA_VALUE, "unexpected value");
    assertEquals(map.remove(null), EXTRA_VALUE, "removed unexpected value");
    // null -> <absent>

    assertFalse(map.containsKey(null), description + ": key present after remove");
    assertNull(map.putIfAbsent(null, null), "previous not null");
    // null -> null
    assertTrue(map.containsKey(null), "null key absent");
    assertNull(map.get(null), "value not null");
    assertNull(map.putIfAbsent(null, EXTRA_VALUE), "previous not null");
    assertEquals(map.get(null), EXTRA_VALUE, "value not expected");
  }

  @Test(dataProvider = "Map<IntegerEnum,String> rw=true keys=all values=all")
  public void testPutIfAbsent(String description, Map<IntegerEnum, String> map) {
    // 1 -> 1
    assertTrue(map.containsKey(KEYS[1]));
    Object expected = map.get(KEYS[1]);
    assertTrue(null == expected || expected.equals(VALUES[1]));
    assertEquals(map.putIfAbsent(KEYS[1], EXTRA_VALUE), expected);
    assertEquals(map.get(KEYS[1]), expected);

    // EXTRA_KEY -> <absent>
    assertFalse(map.containsKey(EXTRA_KEY));
    assertNull(map.putIfAbsent(EXTRA_KEY, EXTRA_VALUE));
    assertEquals(map.get(EXTRA_KEY), EXTRA_VALUE);
    assertEquals(map.putIfAbsent(EXTRA_KEY, VALUES[2]), EXTRA_VALUE);
    assertEquals(map.get(EXTRA_KEY), EXTRA_VALUE);
  }

  @Test(dataProvider = "Map<IntegerEnum,String> rw=all keys=all values=all")
  public void testForEach(String description, Map<IntegerEnum, String> map) {
    IntegerEnum[] EACH_KEY = new IntegerEnum[map.size()];

    map.forEach((k, v) -> {
      int idx = (null == k) ? 0 : k.ordinal(); // substitute for index.
      assertNull(EACH_KEY[idx]);
      EACH_KEY[idx] = (idx == 0) ? KEYS[0] : k; // substitute for comparison.
      assertEquals(v, map.get(k));
    });

    assertEquals(KEYS, EACH_KEY, description);
  }

  @Test(dataProvider = "Map<IntegerEnum,String> rw=true keys=all values=all")
  public static void testReplaceAll(String description, Map<IntegerEnum, String> map) {
    IntegerEnum[] EACH_KEY = new IntegerEnum[map.size()];
    Set<String> EACH_REPLACE = new HashSet<>(map.size());

    map.replaceAll((k, v) -> {
      int idx = (null == k) ? 0 : k.ordinal(); // substitute for index.
      assertNull(EACH_KEY[idx]);
      EACH_KEY[idx] = (idx == 0) ? KEYS[0] : k; // substitute for comparison.
      assertEquals(v, map.get(k));
      String replacement = v + " replaced";
      EACH_REPLACE.add(replacement);
      return replacement;
    });

    assertEquals(KEYS, EACH_KEY, description);
    assertEquals(map.values().size(), EACH_REPLACE.size(), description + EACH_REPLACE);
    assertTrue(EACH_REPLACE.containsAll(map.values()), description + " : " + EACH_REPLACE + " != " + map.values());
    assertTrue(map.values().containsAll(EACH_REPLACE), description + " : " + EACH_REPLACE + " != " + map.values());
  }

  @Test(dataProvider = "Map<IntegerEnum,String> rw=true keys=nonNull values=nonNull")
  public static void testReplaceAllNoNullReplacement(String description, Map<IntegerEnum, String> map) {
    assertThrows(
        () -> {
          map.replaceAll(null);
        },
        NullPointerException.class,
        description);
    assertThrows(
        () -> map.replaceAll((k, v) -> null),
        NullPointerException.class,
        description + " should not allow replacement with null value");
  }

  @Test(dataProvider = "Map<IntegerEnum,String> rw=true keys=withNull values=withNull")
  public static void testRemoveNulls(String description, Map<IntegerEnum, String> map) {
    assertTrue(map.containsKey(null), "null key absent");
    assertNull(map.get(null), "value not null");
    assertFalse(map.remove(null, EXTRA_VALUE), description);
    assertTrue(map.containsKey(null));
    assertNull(map.get(null));
    assertTrue(map.remove(null, null));
    assertFalse(map.containsKey(null));
    assertNull(map.get(null));
    assertFalse(map.remove(null, null));
  }

  @Test(dataProvider = "Map<IntegerEnum,String> rw=true keys=all values=all")
  public static void testRemove(String description, Map<IntegerEnum, String> map) {
    assertTrue(map.containsKey(KEYS[1]));
    Object expected = map.get(KEYS[1]);
    assertTrue(null == expected || expected.equals(VALUES[1]));
    assertFalse(map.remove(KEYS[1], EXTRA_VALUE), description);
    assertEquals(map.get(KEYS[1]), expected);
    assertTrue(map.remove(KEYS[1], expected));
    assertNull(map.get(KEYS[1]));
    assertFalse(map.remove(KEYS[1], expected));

    assertFalse(map.containsKey(EXTRA_KEY));
    assertFalse(map.remove(EXTRA_KEY, EXTRA_VALUE));
  }

  @Test(dataProvider = "Map<IntegerEnum,String> rw=true keys=withNull values=withNull")
  public void testReplaceKVNulls(String description, Map<IntegerEnum, String> map) {
    assertTrue(map.containsKey(null), "null key absent");
    assertNull(map.get(null), "value not null");
    assertNull(map.replace(null, EXTRA_VALUE));
    assertEquals(map.get(null), EXTRA_VALUE);
  }

  @Test(dataProvider = "Map<IntegerEnum,String> rw=true keys=nonNull values=nonNull")
  public void testReplaceKVNoNulls(String description, Map<IntegerEnum, String> map) {
    assertTrue(map.containsKey(FIRST_KEY), "expected key missing");
    assertEquals(map.get(FIRST_KEY), FIRST_VALUE, "found wrong value");
    assertThrows(() -> map.replace(FIRST_KEY, null), NullPointerException.class, description + ": should throw NPE");
    assertEquals(map.replace(FIRST_KEY, EXTRA_VALUE), FIRST_VALUE, description + ": replaced wrong value");
    assertEquals(map.get(FIRST_KEY), EXTRA_VALUE, "found wrong value");
  }

  @Test(dataProvider = "Map<IntegerEnum,String> rw=true keys=all values=all")
  public void testReplaceKV(String description, Map<IntegerEnum, String> map) {
    assertTrue(map.containsKey(KEYS[1]));
    Object expected = map.get(KEYS[1]);
    assertTrue(null == expected || expected.equals(VALUES[1]));
    assertEquals(map.replace(KEYS[1], EXTRA_VALUE), expected);
    assertEquals(map.get(KEYS[1]), EXTRA_VALUE);

    assertFalse(map.containsKey(EXTRA_KEY));
    assertNull(map.replace(EXTRA_KEY, EXTRA_VALUE));
    assertFalse(map.containsKey(EXTRA_KEY));
    assertNull(map.get(EXTRA_KEY));
    assertNull(map.put(EXTRA_KEY, EXTRA_VALUE));
    assertEquals(map.get(EXTRA_KEY), EXTRA_VALUE);
    assertEquals(map.replace(EXTRA_KEY, (String) expected), EXTRA_VALUE);
    assertEquals(map.get(EXTRA_KEY), expected);
  }

  @Test(dataProvider = "Map<IntegerEnum,String> rw=true keys=withNull values=withNull")
  public void testReplaceKVVNulls(String description, Map<IntegerEnum, String> map) {
    assertTrue(map.containsKey(null), "null key absent");
    assertNull(map.get(null), "value not null");
    assertFalse(map.replace(null, EXTRA_VALUE, EXTRA_VALUE));
    assertNull(map.get(null));
    assertTrue(map.replace(null, null, EXTRA_VALUE));
    assertEquals(map.get(null), EXTRA_VALUE);
    assertTrue(map.replace(null, EXTRA_VALUE, EXTRA_VALUE));
    assertEquals(map.get(null), EXTRA_VALUE);
  }

  @Test(dataProvider = "Map<IntegerEnum,String> rw=true keys=nonNull values=nonNull")
  public void testReplaceKVVNoNulls(String description, Map<IntegerEnum, String> map) {
    assertTrue(map.containsKey(FIRST_KEY), "expected key missing");
    assertEquals(map.get(FIRST_KEY), FIRST_VALUE, "found wrong value");
    assertThrows(() -> map.replace(FIRST_KEY, FIRST_VALUE, null), NullPointerException.class, description + ": should throw NPE");
    assertThrows(() -> {
      if (!map.replace(FIRST_KEY, null, EXTRA_VALUE))
        throw new NullPointerException("default returns false rather than throwing");
    }, NullPointerException.class, description + ": should throw NPE");
    assertTrue(map.replace(FIRST_KEY, FIRST_VALUE, EXTRA_VALUE), description + ": replaced wrong value");
    assertEquals(map.get(FIRST_KEY), EXTRA_VALUE, "found wrong value");
  }

  @Test(dataProvider = "Map<IntegerEnum,String> rw=true keys=all values=all")
  public void testReplaceKVV(String description, Map<IntegerEnum, String> map) {
    assertTrue(map.containsKey(KEYS[1]));
    Object expected = map.get(KEYS[1]);
    assertTrue(null == expected || expected.equals(VALUES[1]));
    assertFalse(map.replace(KEYS[1], EXTRA_VALUE, EXTRA_VALUE));
    assertEquals(map.get(KEYS[1]), expected);
    assertTrue(map.replace(KEYS[1], (String) expected, EXTRA_VALUE));
    assertEquals(map.get(KEYS[1]), EXTRA_VALUE);
    assertTrue(map.replace(KEYS[1], EXTRA_VALUE, EXTRA_VALUE));
    assertEquals(map.get(KEYS[1]), EXTRA_VALUE);

    assertFalse(map.containsKey(EXTRA_KEY));
    assertFalse(map.replace(EXTRA_KEY, EXTRA_VALUE, EXTRA_VALUE));
    assertFalse(map.containsKey(EXTRA_KEY));
    assertNull(map.get(EXTRA_KEY));
    assertNull(map.put(EXTRA_KEY, EXTRA_VALUE));
    assertTrue(map.containsKey(EXTRA_KEY));
    assertEquals(map.get(EXTRA_KEY), EXTRA_VALUE);
    assertTrue(map.replace(EXTRA_KEY, EXTRA_VALUE, EXTRA_VALUE));
    assertEquals(map.get(EXTRA_KEY), EXTRA_VALUE);
  }

  @Test(dataProvider = "Map<IntegerEnum,String> rw=true keys=withNull values=withNull")
  public void testComputeIfAbsentNulls(String description, Map<IntegerEnum, String> map) {
    // null -> null
    assertTrue(map.containsKey(null), "null key absent");
    assertNull(map.get(null), "value not null");
    assertNull(map.computeIfAbsent(null, (k) -> null), "not expected result");
    assertTrue(map.containsKey(null), "null key absent");
    assertNull(map.get(null), "value not null");
    assertEquals(map.computeIfAbsent(null, (k) -> EXTRA_VALUE), EXTRA_VALUE, "not mapped to result");
    // null -> EXTRA_VALUE
    assertTrue(map.containsKey(null), "null key absent");
    assertEquals(map.get(null), EXTRA_VALUE, "not expected value");
    assertEquals(map.remove(null), EXTRA_VALUE, "removed unexpected value");
    // null -> <absent>
    assertFalse(map.containsKey(null), "null key present");
    assertEquals(map.computeIfAbsent(null, (k) -> EXTRA_VALUE), EXTRA_VALUE, "not mapped to result");
    // null -> EXTRA_VALUE
    assertTrue(map.containsKey(null), "null key absent");
    assertEquals(map.get(null), EXTRA_VALUE, "not expected value");
  }

  @Test(dataProvider = "Map<IntegerEnum,String> rw=true keys=all values=all")
  public void testComputeIfAbsent(String description, Map<IntegerEnum, String> map) {
    // 1 -> 1
    assertTrue(map.containsKey(KEYS[1]));
    Object expected = map.get(KEYS[1]);
    assertTrue(null == expected || expected.equals(VALUES[1]), description + expected);
    expected = (null == expected) ? EXTRA_VALUE : expected;
    assertEquals(map.computeIfAbsent(KEYS[1], (k) -> EXTRA_VALUE), expected, description);
    assertEquals(map.get(KEYS[1]), expected, description);

    // EXTRA_KEY -> <absent>
    assertFalse(map.containsKey(EXTRA_KEY));
    assertNull(map.computeIfAbsent(EXTRA_KEY, (k) -> null));
    assertFalse(map.containsKey(EXTRA_KEY));
    assertEquals(map.computeIfAbsent(EXTRA_KEY, (k) -> EXTRA_VALUE), EXTRA_VALUE);
    // EXTRA_KEY -> EXTRA_VALUE
    assertEquals(map.get(EXTRA_KEY), EXTRA_VALUE);
  }

  @Test(dataProvider = "Map<IntegerEnum,String> rw=true keys=all values=all")
  public void testComputeIfAbsentNullFunction(String description, Map<IntegerEnum, String> map) {
    assertThrows(() -> map.computeIfAbsent(KEYS[1], null),
        NullPointerException.class,
        "Should throw NPE");
  }

  @Test(dataProvider = "Map<IntegerEnum,String> rw=true keys=withNull values=withNull")
  public void testComputeIfPresentNulls(String description, Map<IntegerEnum, String> map) {
    assertTrue(map.containsKey(null), description + ": null key absent");
    assertNull(map.get(null), description + ": value not null");
    assertNull(map.computeIfPresent(null, (k, v) -> {
      fail(description + ": null value is not deemed present");
      return EXTRA_VALUE;
    }), description);
    assertTrue(map.containsKey(null));
    assertNull(map.get(null), description);
    assertNull(map.remove(EXTRA_KEY), description + ": unexpected mapping");
    assertNull(map.put(EXTRA_KEY, null), description + ": unexpected value");
    assertNull(map.computeIfPresent(EXTRA_KEY, (k, v) -> {
      fail(description + ": null value is not deemed present");
      return EXTRA_VALUE;
    }), description);
    assertNull(map.get(EXTRA_KEY), description + ": null mapping gone");
  }

  @Test(dataProvider = "Map<IntegerEnum,String> rw=true keys=all values=all")
  public void testComputeIfPresent(String description, Map<IntegerEnum, String> map) {
    assertTrue(map.containsKey(KEYS[1]));
    Object value = map.get(KEYS[1]);
    assertTrue(null == value || value.equals(VALUES[1]), description + value);
    Object expected = (null == value) ? null : EXTRA_VALUE;
    assertEquals(map.computeIfPresent(KEYS[1], (k, v) -> {
      assertEquals(v, value);
      return EXTRA_VALUE;
    }), expected, description);
    assertEquals(map.get(KEYS[1]), expected, description);

    assertFalse(map.containsKey(EXTRA_KEY));
    assertNull(map.computeIfPresent(EXTRA_KEY, (k, v) -> {
      fail();
      return EXTRA_VALUE;
    }));
    assertFalse(map.containsKey(EXTRA_KEY));
    assertNull(map.get(EXTRA_KEY));
  }

  @Test(dataProvider = "Map<IntegerEnum,String> rw=true keys=all values=all")
  public void testComputeIfPresentNullFunction(String description, Map<IntegerEnum, String> map) {
    assertThrows(() -> map.computeIfPresent(KEYS[1], null),
        NullPointerException.class,
        "Should throw NPE");
  }

  @Test(dataProvider = "Map<IntegerEnum,String> rw=true keys=withNull values=withNull")
  public void testComputeNulls(String description, Map<IntegerEnum, String> map) {
    assertTrue(map.containsKey(null), "null key absent");
    assertNull(map.get(null), "value not null");
    assertNull(map.compute(null, (k, v) -> {
      assertNull(k);
      assertNull(v);
      return null;
    }), description);
    assertFalse(map.containsKey(null), description + ": null key present.");
    assertEquals(map.compute(null, (k, v) -> {
      assertNull(k);
      assertNull(v);
      return EXTRA_VALUE;
    }), EXTRA_VALUE, description);
    assertTrue(map.containsKey(null));
    assertEquals(map.get(null), EXTRA_VALUE, description);
    assertEquals(map.remove(null), EXTRA_VALUE, description + ": removed value not expected");
    // no mapping before and after
    assertFalse(map.containsKey(null), description + ": null key present");
    assertNull(map.compute(null, (k, v) -> {
      assertNull(k);
      assertNull(v);
      return null;
    }), description + ": expected null result");
    assertFalse(map.containsKey(null), description + ": null key present");
    // compute with map not containing value
    assertNull(map.remove(EXTRA_KEY), description + ": unexpected mapping");
    assertFalse(map.containsKey(EXTRA_KEY), description + ": key present");
    assertNull(map.compute(EXTRA_KEY, (k, v) -> {
      assertEquals(k, EXTRA_KEY);
      assertNull(v);
      return null;
    }), description);
    assertFalse(map.containsKey(EXTRA_KEY), description + ": null key present");
    // ensure removal.
    assertNull(map.put(EXTRA_KEY, EXTRA_VALUE));
    assertNull(map.compute(EXTRA_KEY, (k, v) -> {
      assertEquals(k, EXTRA_KEY);
      assertEquals(v, EXTRA_VALUE);
      return null;
    }), description + ": null resulted expected");
    assertFalse(map.containsKey(EXTRA_KEY), description + ": null key present");
    // compute with map containing null value
    assertNull(map.put(EXTRA_KEY, null), description + ": unexpected value");
    assertNull(map.compute(EXTRA_KEY, (k, v) -> {
      assertEquals(k, EXTRA_KEY);
      assertNull(v);
      return null;
    }), description);
    assertFalse(map.containsKey(EXTRA_KEY), description + ": null key present");
    assertNull(map.put(EXTRA_KEY, null), description + ": unexpected value");
    assertEquals(map.compute(EXTRA_KEY, (k, v) -> {
      assertEquals(k, EXTRA_KEY);
      assertNull(v);
      return EXTRA_VALUE;
    }), EXTRA_VALUE, description);
    assertTrue(map.containsKey(EXTRA_KEY), "null key present");
  }

  @Test(dataProvider = "Map<IntegerEnum,String> rw=true keys=all values=all")
  public void testCompute(String description, Map<IntegerEnum, String> map) {
    assertTrue(map.containsKey(KEYS[1]));
    Object value = map.get(KEYS[1]);
    assertTrue(null == value || value.equals(VALUES[1]), description + value);
    assertEquals(map.compute(KEYS[1], (k, v) -> {
      assertEquals(k, KEYS[1]);
      assertEquals(v, value);
      return EXTRA_VALUE;
    }), EXTRA_VALUE, description);
    assertEquals(map.get(KEYS[1]), EXTRA_VALUE, description);
    assertNull(map.compute(KEYS[1], (k, v) -> {
      assertEquals(v, EXTRA_VALUE);
      return null;
    }), description);
    assertFalse(map.containsKey(KEYS[1]));

    assertFalse(map.containsKey(EXTRA_KEY));
    assertEquals(map.compute(EXTRA_KEY, (k, v) -> {
      assertNull(v);
      return EXTRA_VALUE;
    }), EXTRA_VALUE);
    assertTrue(map.containsKey(EXTRA_KEY));
    assertEquals(map.get(EXTRA_KEY), EXTRA_VALUE);
  }

  @Test(dataProvider = "Map<IntegerEnum,String> rw=true keys=all values=all")
  public void testComputeNullFunction(String description, Map<IntegerEnum, String> map) {
    assertThrows(() -> map.compute(KEYS[1], null),
        NullPointerException.class,
        "Should throw NPE");
  }

  @Test(dataProvider = "MergeCases")
  private void testMerge(String description, Map<IntegerEnum, String> map, Merging.Value oldValue, Merging.Value newValue, Merging.Merger merger, Merging.Value put, Merging.Value result) {
    // add and check initial conditions.
    switch (oldValue) {
      case ABSENT:
        map.remove(EXTRA_KEY);
        assertFalse(map.containsKey(EXTRA_KEY), "key not absent");
        break;
      case NULL:
        map.put(EXTRA_KEY, null);
        assertTrue(map.containsKey(EXTRA_KEY), "key absent");
        assertNull(map.get(EXTRA_KEY), "wrong value");
        break;
      case OLDVALUE:
        map.put(EXTRA_KEY, VALUES[1]);
        assertTrue(map.containsKey(EXTRA_KEY), "key absent");
        assertEquals(map.get(EXTRA_KEY), VALUES[1], "wrong value");
        break;
      default:
        fail("unexpected old value");
    }

    String returned = map.merge(EXTRA_KEY,
        newValue == Merging.Value.NULL ? (String) null : VALUES[2],
        merger
    );

    // check result

    switch (result) {
      case NULL:
        assertNull(returned, "wrong value");
        break;
      case NEWVALUE:
        assertEquals(returned, VALUES[2], "wrong value");
        break;
      case RESULT:
        assertEquals(returned, VALUES[3], "wrong value");
        break;
      default:
        fail("unexpected new value");
    }

    // check map
    switch (put) {
      case ABSENT:
        assertFalse(map.containsKey(EXTRA_KEY), "key not absent");
        break;
      case NULL:
        assertTrue(map.containsKey(EXTRA_KEY), "key absent");
        assertNull(map.get(EXTRA_KEY), "wrong value");
        break;
      case NEWVALUE:
        assertTrue(map.containsKey(EXTRA_KEY), "key absent");
        assertEquals(map.get(EXTRA_KEY), VALUES[2], "wrong value");
        break;
      case RESULT:
        assertTrue(map.containsKey(EXTRA_KEY), "key absent");
        assertEquals(map.get(EXTRA_KEY), VALUES[3], "wrong value");
        break;
      default:
        fail("unexpected new value");
    }
  }

  @Test(dataProvider = "Map<IntegerEnum,String> rw=true keys=all values=all")
  public void testMergeNullMerger(String description, Map<IntegerEnum, String> map) {
    assertThrows(() -> map.merge(KEYS[1], VALUES[1], null),
        NullPointerException.class,
        "Should throw NPE");
  }

  private static final int TEST_SIZE = IntegerEnum.SIZE - 1;
  /**
   * Realized keys ensure that there is always a hard ref to all test objects.
   */
  private static final IntegerEnum[] KEYS = new IntegerEnum[TEST_SIZE];
  /**
   * Realized values ensure that there is always a hard ref to all test
   * objects.
   */
  private static final String[] VALUES = new String[TEST_SIZE];

  static {
    IntegerEnum[] keys = IntegerEnum.values();
    for (int each = 0; each < TEST_SIZE; each++) {
      KEYS[each] = keys[each];
      VALUES[each] = String.valueOf(each);
    }
  }

  private static final IntegerEnum FIRST_KEY = KEYS[0];
  private static final String FIRST_VALUE = VALUES[0];
  private static final IntegerEnum EXTRA_KEY = IntegerEnum.EXTRA_KEY;
  private static final String EXTRA_VALUE = String.valueOf(TEST_SIZE);

  @DataProvider(name = "Map<IntegerEnum,String> rw=all keys=all values=all", parallel = true)
  public static Iterator<Object[]> allMapProvider() {
    return makeAllMaps().iterator();
  }

  @DataProvider(name = "Map<IntegerEnum,String> rw=all keys=withNull values=withNull", parallel = true)
  public static Iterator<Object[]> allMapWithNullsProvider() {
    return makeAllMapsWithNulls().iterator();
  }

  @DataProvider(name = "Map<IntegerEnum,String> rw=true keys=nonNull values=nonNull", parallel = true)
  public static Iterator<Object[]> rwNonNullMapProvider() {
    return makeRWNoNullsMaps().iterator();
  }

  @DataProvider(name = "Map<IntegerEnum,String> rw=true keys=nonNull values=all", parallel = true)
  public static Iterator<Object[]> rwNonNullKeysMapProvider() {
    return makeRWMapsNoNulls().iterator();
  }

  @DataProvider(name = "Map<IntegerEnum,String> rw=true keys=all values=all", parallel = true)
  public static Iterator<Object[]> rwMapProvider() {
    return makeAllRWMaps().iterator();
  }

  @DataProvider(name = "Map<IntegerEnum,String> rw=true keys=withNull values=withNull", parallel = true)
  public static Iterator<Object[]> rwNullsMapProvider() {
    return makeAllRWMapsWithNulls().iterator();
  }

  private static Collection<Object[]> makeAllRWMapsWithNulls() {
    Collection<Object[]> all = new ArrayList<>();

    all.addAll(makeRWMaps(true, true));

    return all;
  }


  private static Collection<Object[]> makeRWMapsNoNulls() {
    Collection<Object[]> all = new ArrayList<>();

    all.addAll(makeRWNoNullKeysMaps(false));
    all.addAll(makeRWNoNullsMaps());

    return all;
  }

  private static Collection<Object[]> makeAllROMaps() {
    Collection<Object[]> all = new ArrayList<>();

    all.addAll(makeROMaps(false));
    all.addAll(makeROMaps(true));

    return all;
  }

  private static Collection<Object[]> makeAllRWMaps() {
    Collection<Object[]> all = new ArrayList<>();

    all.addAll(makeRWNoNullsMaps());
    all.addAll(makeRWMaps(false, true));
    all.addAll(makeRWMaps(true, true));
    all.addAll(makeRWNoNullKeysMaps(true));
    return all;
  }

  private static Collection<Object[]> makeAllMaps() {
    Collection<Object[]> all = new ArrayList<>();

    all.addAll(makeAllROMaps());
    all.addAll(makeAllRWMaps());

    return all;
  }

  private static Collection<Object[]> makeAllMapsWithNulls() {
    Collection<Object[]> all = new ArrayList<>();

    all.addAll(makeROMaps(true));
    all.addAll(makeRWMaps(true, true));

    return all;
  }

  /**
   * @param nullKeys   include null keys
   * @param nullValues include null values
   */
  private static Collection<Object[]> makeRWMaps(boolean nullKeys, boolean nullValues) {
    return Arrays.asList(
        new Object[]{"HashMap", makeMap(HashMap::new, nullKeys, nullValues)},
        new Object[]{"RedisMap", makeMap(RedisMap::new, nullKeys, nullValues)},
        new Object[]{"IdentityHashMap", makeMap(IdentityHashMap::new, nullKeys, nullValues)},
        new Object[]{"LinkedHashMap", makeMap(LinkedHashMap::new, nullKeys, nullValues)},
        new Object[]{"WeakHashMap", makeMap(WeakHashMap::new, nullKeys, nullValues)},
        new Object[]{"Collections.checkedMap(HashMap)", Collections.checkedMap(makeMap(HashMap::new, nullKeys, nullValues), IntegerEnum.class, String.class)},
        new Object[]{"Collections.synchronizedMap(HashMap)", Collections.synchronizedMap(makeMap(HashMap::new, nullKeys, nullValues))},
        new Object[]{"ExtendsAbstractMap", makeMap(ExtendsAbstractMap::new, nullKeys, nullValues)});
  }

  /**
   * @param nulls include null values
   */
  private static Collection<Object[]> makeRWNoNullKeysMaps(boolean nulls) {
    return Arrays.asList(
        // null key hostile
        new Object[]{"EnumMap", makeMap(() -> new EnumMap(IntegerEnum.class), false, nulls)},
        new Object[]{"TreeMap", makeMap(TreeMap::new, false, nulls)},
        new Object[]{"ExtendsAbstractMap(TreeMap)", makeMap(() -> {
          return new ExtendsAbstractMap(new TreeMap());
        }, false, nulls)},
        new Object[]{"Collections.synchronizedMap(EnumMap)", Collections.synchronizedMap(makeMap(() -> new EnumMap(IntegerEnum.class), false, nulls))}
    );
  }

  private static Collection<Object[]> makeRWNoNullsMaps() {
    return Arrays.asList(
        // null key and value hostile
        new Object[]{"Hashtable", makeMap(Hashtable::new, false, false)},
        new Object[]{"ConcurrentHashMap", makeMap(ConcurrentHashMap::new, false, false)},
        new Object[]{"ConcurrentSkipListMap", makeMap(ConcurrentSkipListMap::new, false, false)},
        new Object[]{"Collections.synchronizedMap(ConcurrentHashMap)", Collections.synchronizedMap(makeMap(ConcurrentHashMap::new, false, false))},
        new Object[]{"Collections.checkedMap(ConcurrentHashMap)", Collections.checkedMap(makeMap(ConcurrentHashMap::new, false, false), IntegerEnum.class, String.class)},
        new Object[]{"ExtendsAbstractMap(ConcurrentHashMap)", makeMap(() -> {
          return new ExtendsAbstractMap(new ConcurrentHashMap());
        }, false, false)},
        new Object[]{"ImplementsConcurrentMap", makeMap(ImplementsConcurrentMap::new, false, false)}
    );
  }

  /**
   * @param nulls include nulls
   */
  private static Collection<Object[]> makeROMaps(boolean nulls) {
    return Arrays.asList(new Object[][]{
        new Object[]{"Collections.unmodifiableMap(HashMap)", Collections.unmodifiableMap(makeMap(HashMap::new, nulls, nulls))}
    });
  }

  /**
   * @param supplier   a supplier of mutable map instances.
   * @param nullKeys   include null keys
   * @param nullValues include null values
   */
  private static Map<IntegerEnum, String> makeMap(Supplier<Map<IntegerEnum, String>> supplier, boolean nullKeys, boolean nullValues) {
    Map<IntegerEnum, String> result = supplier.get();

    for (int each = 0; each < TEST_SIZE; each++) {
      IntegerEnum key = nullKeys ? (each == 0) ? null : KEYS[each] : KEYS[each];
      String value = nullValues ? (each == 0) ? null : VALUES[each] : VALUES[each];

      result.put(key, value);
    }

    return result;
  }

  static class Merging {
    public enum Value {
      ABSENT,
      NULL,
      OLDVALUE,
      NEWVALUE,
      RESULT
    }

    public enum Merger implements BiFunction<String, String, String> {
      UNUSED {
        public String apply(String oldValue, String newValue) {
          fail("should not be called");
          return null;
        }
      },
      NULL {
        public String apply(String oldValue, String newValue) {
          return null;
        }
      },
      RESULT {
        public String apply(String oldValue, String newValue) {
          return VALUES[3];
        }
      },
    }
  }

  @DataProvider(name = "MergeCases", parallel = true)
  public Iterator<Object[]> mergeCasesProvider() {
    Collection<Object[]> cases = new ArrayList<>();

    cases.addAll(makeMergeTestCases());

    return cases.iterator();
  }

  static Collection<Object[]> makeMergeTestCases() {
    Collection<Object[]> cases = new ArrayList<>();

    for (Object[] mapParams : makeAllRWMaps()) {
      cases.add(new Object[]{mapParams[0], mapParams[1], Merging.Value.ABSENT, Merging.Value.NEWVALUE, Merging.Merger.UNUSED, Merging.Value.NEWVALUE, Merging.Value.NEWVALUE});
    }

    for (Object[] mapParams : makeAllRWMaps()) {
      cases.add(new Object[]{mapParams[0], mapParams[1], Merging.Value.OLDVALUE, Merging.Value.NEWVALUE, Merging.Merger.NULL, Merging.Value.ABSENT, Merging.Value.NULL});
    }

    for (Object[] mapParams : makeAllRWMaps()) {
      cases.add(new Object[]{mapParams[0], mapParams[1], Merging.Value.OLDVALUE, Merging.Value.NEWVALUE, Merging.Merger.RESULT, Merging.Value.RESULT, Merging.Value.RESULT});
    }

    return cases;
  }

  public interface Thrower<T extends Throwable> {

    void run() throws T;
  }

  public static <T extends Throwable> void assertThrows(Thrower<T> thrower, Class<T> throwable) {
    assertThrows(thrower, throwable, null);
  }

  public static <T extends Throwable> void assertThrows(Thrower<T> thrower, Class<T> throwable, String message) {
    Throwable thrown;
    try {
      thrower.run();
      thrown = null;
    } catch (Throwable caught) {
      thrown = caught;
    }

    assertInstance(thrown, throwable,
        ((null != message) ? message : "") +
            " Failed to throw " + throwable.getCanonicalName());
  }

  @SafeVarargs
  public static <T extends Throwable> void assertThrows(Class<T> throwable, String message, Thrower<T>... throwers) {
    for (Thrower<T> thrower : throwers) {
      assertThrows(thrower, throwable, message);
    }
  }

  public static void assertInstance(Object actual, Class<?> expected) {
    assertInstance(expected.isInstance(actual), null);
  }

  public static void assertInstance(Object actual, Class<?> expected, String message) {
    assertTrue(expected.isInstance(actual), message);
  }

  /**
   * A simple mutable map implementation that provides only default
   * implementations of all methods. ie. none of the Map interface default
   * methods have overridden implementations.
   *
   * @param <K> Type of keys
   * @param <V> Type of values
   */
  @SuppressWarnings("unchecked")
  public static class ExtendsAbstractMap<M extends Map<K, V>, K, V> extends AbstractMap<K, V> {

    protected final M map;

    public ExtendsAbstractMap() {
      this((M) new HashMap<K, V>());
    }

    protected ExtendsAbstractMap(M map) {
      this.map = map;
    }

    public Set<Map.Entry<K, V>> entrySet() {
      return new AbstractSet<Map.Entry<K, V>>() {
        public int size() {
          return map.size();
        }

        public Iterator<Map.Entry<K, V>> iterator() {
          final Iterator<Map.Entry<K, V>> source = map.entrySet().iterator();
          return new Iterator<Map.Entry<K, V>>() {
            public boolean hasNext() {
              return source.hasNext();
            }

            public Map.Entry<K, V> next() {
              return source.next();
            }

            public void remove() {
              source.remove();
            }
          };
        }

        public boolean add(Map.Entry<K, V> e) {
          return map.entrySet().add(e);
        }
      };
    }

    public V put(K key, V value) {
      return map.put(key, value);
    }
  }

  /**
   * A simple mutable concurrent map implementation that provides only default
   * implementations of all methods. ie. none of the ConcurrentMap interface
   * default methods have overridden implementations.
   *
   * @param <K> Type of keys
   * @param <V> Type of values
   */
  public static class ImplementsConcurrentMap<K, V> extends ExtendsAbstractMap<ConcurrentMap<K, V>, K, V> implements ConcurrentMap<K, V> {
    public ImplementsConcurrentMap() {
      super(new ConcurrentHashMap<>());
    }

    // ConcurrentMap reabstracts these methods

    public V replace(K k, V v) {
      return map.replace(k, v);
    }

    public boolean replace(K k, V v, V vv) {
      return map.replace(k, v, vv);
    }

    public boolean remove(Object k, Object v) {
      return map.remove(k, v);
    }

    public V putIfAbsent(K k, V v) {
      return map.putIfAbsent(k, v);
    }
  }
}