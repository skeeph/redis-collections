package testlibrary;

import java.lang.Exception;
import java.lang.Integer;
import java.lang.Iterable;
import java.lang.Override;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import org.testng.TestException;

import static org.testng.Assert.assertTrue;

import java.util.Collection;
import java.util.Collections;
import java.util.function.Supplier;

/**
 * @library
 * @summary A Supplier of test cases for Collection tests
 */
@SuppressWarnings({"unchecked", "JavaDoc"})
public final class CollectionSupplier<C extends Collection<Integer>> implements Supplier<Iterable<CollectionSupplier.TestCase<C>>> {

  private final Supplier<C>[] classes;
  private final int size;

  /**
   * A Collection test case.
   */
  @SuppressWarnings("WeakerAccess")
  public static final class TestCase<C extends Collection<Integer>> {

    /**
     * The name of the test case.
     */
    public final String name;

    /**
     * Unmodifiable reference collection, useful for comparisons.
     */
    public final List<Integer> expected;

    /**
     * A modifiable test collection.
     */
    public final C collection;

    /**
     * Create a Collection test case.
     *
     * @param name name of the test case
     * @param collection the modifiable test collection
     */
    public TestCase(String name, C collection) {
      this.name = name;
      this.expected = Collections.unmodifiableList(
          Arrays.asList(collection.toArray(new Integer[0])));
      this.collection = collection;
    }

    @Override
    public String toString() {
      return name + " " + collection.getClass().toString();
    }
  }

  /**
   * Shuffle a list using a PRNG with known seed for repeatability
   *
   * @param list the list to be shuffled
   */
  public static <E> void shuffle(final List<E> list) {
    // PRNG with known seed for repeatable tests
    final Random prng = new Random(13);
    final int size = list.size();
    for (int i = 0; i < size; i++) {
      // random index in interval [i, size)
      final int j = i + prng.nextInt(size - i);
      // swap elements at indices i & j
      final E e = list.get(i);
      list.set(i, list.get(j));
      list.set(j, e);
    }
  }

  /**
   * Create a {@code Supplier} that creates instances of specified collection
   * classes of specified length.
   *
   * @param size the desired size of each collection
   */
  public CollectionSupplier(Supplier<C>[] classes, int size) {
    this.classes = Arrays.copyOf(classes, classes.length);
    this.size = size;
  }

  @Override
  public Iterable<TestCase<C>> get() {
    final Collection<TestCase<C>> cases = new LinkedList<>();
    for (final Supplier<C> type : classes) {
      try {
        final Collection<Integer> empty = type.get();
        cases.add(new TestCase("empty", empty));

        final Collection<Integer> single = type.get();
        single.add(42);
        cases.add(new TestCase("single", single));

        final Collection<Integer> regular = type.get();
        for (int i = 0; i < size; i++) {
          regular.add(i);
        }
        cases.add(new TestCase("regular", regular));

        final Collection<Integer> reverse = type.get();
        for (int i = size; i >= 0; i--) {
          reverse.add(i);
        }
        cases.add(new TestCase("reverse", reverse));

        final Collection<Integer> odds = type.get();
        for (int i = 0; i < size; i++) {
          odds.add((i * 2) + 1);
        }
        cases.add(new TestCase("odds", odds));

        final Collection<Integer> evens = type.get();
        for (int i = 0; i < size; i++) {
          evens.add(i * 2);
        }
        cases.add(new TestCase("evens", evens));

        final Collection<Integer> fibonacci = type.get();
        int prev2 = 0;
        int prev1 = 1;
        for (int i = 0; i < size; i++) {
          final int n = prev1 + prev2;
          if (n < 0) { // stop on overflow
            break;
          }
          fibonacci.add(n);
          prev2 = prev1;
          prev1 = n;
        }
        cases.add(new TestCase("fibonacci", fibonacci));

        // variants where the size of the backing storage != reported size
        // created by removing half of the elements
        final Collection<Integer> emptyWithSlack = type.get();
        emptyWithSlack.add(42);
        assertTrue(emptyWithSlack.remove(42));
        cases.add(new TestCase("emptyWithSlack", emptyWithSlack));

        final Collection<Integer> regularWithSlack = type.get(); //TODO
        for (int i = 0; i < (2 * size); i++) {
          regularWithSlack.add(i);
        }
        assertTrue(regularWithSlack.removeIf((x) -> x >= size));
        cases.add(new TestCase("regularWithSlack", regularWithSlack));

        final Collection<Integer> reverseWithSlack = type.get();
        for (int i = 2 * size; i >= 0; i--) {
          reverseWithSlack.add(i);
        }
        assertTrue(reverseWithSlack.removeIf((x) -> x < size));
        cases.add(new TestCase("reverseWithSlack", reverseWithSlack));

        final Collection<Integer> oddsWithSlack = type.get();
        for (int i = 0; i < 2 * size; i++) {
          oddsWithSlack.add((i * 2) + 1);
        }
        assertTrue(oddsWithSlack.removeIf((x) -> x >= size));
        cases.add(new TestCase("oddsWithSlack", oddsWithSlack));

        final Collection<Integer> evensWithSlack = type.get();
        for (int i = 0; i < 2 * size; i++) {
          evensWithSlack.add(i * 2);
        }
        assertTrue(evensWithSlack.removeIf((x) -> x >= size));
        cases.add(new TestCase("evensWithSlack", evensWithSlack));

        final Collection<Integer> fibonacciWithSlack = type.get();
        prev2 = 0;
        prev1 = 1;
        for (int i = 0; i < size; i++) {
          final int n = prev1 + prev2;
          if (n < 0) { // stop on overflow
            break;
          }
          fibonacciWithSlack.add(n);
          prev2 = prev1;
          prev1 = n;
        }
        assertTrue(fibonacciWithSlack.removeIf((x) -> x < 20));
        cases.add(new TestCase("fibonacciWithSlack",
            fibonacciWithSlack));
      } catch (Exception failed) {
        throw new TestException(failed);
      }
    }

    return cases;
  }

}