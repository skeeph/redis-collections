package redis;

import redis.clients.jedis.Jedis;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class RedisList<E> implements List<E> {
  private Jedis jedis;
  private String sizeKey;
  private Coder<Integer, E> coder;

  public RedisList() {
    //TODO: Добавить настройки
    jedis = new Jedis();
    String listName = Long.toString(System.currentTimeMillis());
    sizeKey = listName + "/size";
    coder = new ToStringCoder<>(listName);
  }

  public RedisList(Collection<? extends E> data) {
    this();
    addAll(data);
  }

  @Override
  public int size() {
    String sizeVal = jedis.get(sizeKey);
    return Integer.parseInt(sizeVal == null ? "0" : sizeVal);
  }

  @Override
  public boolean isEmpty() {
    return size() == 0;
  }

  @Override
  public boolean contains(Object o) {
    if (size() == 0) return false;
    for (E e : this) {
      if (Objects.equals(e, o)) return true;
    }
    return false;
  }

  @Override
  public Iterator<E> iterator() {
    return new RedisIterator();
  }

  @Override
  public Object[] toArray() {
    Object[] res = new Object[size()];
    for (int i = 0; i < size(); i++) res[i] = get(i);
    return res;
  }

  @Override
  public <T> T[] toArray(T[] a) {
    if (a.length < size())
      // Make a new array of a's runtime type, but my contents:
      return (T[]) Arrays.copyOf(toArray(), size(), a.getClass());
    System.arraycopy(toArray(), 0, a, 0, size());
    if (a.length > size())
      a[size()] = null;
    return a;
  }

  @Override
  public boolean add(E s) {
    int key = size();
    jedis.set(coder.encodeKey(key), coder.encodeValue(s));
    incSize();
    //TODO: murtuzaaliev 03.09.2019
    return true;
  }

  @Override
  public boolean remove(Object o) {
    for (int i = 0; i < size(); i++) {
      E e = get(i);
      if (Objects.equals(e, o)) {
        jedis.del(coder.encodeKey(i));
        shiftLeft(i);
        return true;
      }
    }
    return false;
  }

  private void shiftLeft(int k) {
    for (int i = k; i < size() - 1; i++) {
      String next = jedis.get(coder.encodeKey(i + 1));
      jedis.set(coder.encodeKey(i), next);
    }
    jedis.del(coder.encodeKey(size() - 1));
    decSize();
  }

  private void shiftRight(int index) {
    incSize();
    for (int i = size() - 1; i > index; i--) {
      String prev = jedis.get(coder.encodeKey(i - 1));
      jedis.set(coder.encodeKey(i), prev);
    }
  }

  @Override
  public boolean containsAll(Collection<?> c) {
    for (Object o : c) {
      if (!contains(o)) return false;
    }
    return true;
  }

  @Override
  public boolean addAll(Collection<? extends E> c) {
    boolean changed = false;
    for (E e : c) {
      changed = add(e) || changed;
    }
    return changed;
  }

  @Override
  public boolean addAll(int index, Collection<? extends E> c) {
    boolean changed = false; //TODO: murtuzaaliev 01.09.2019 check changed
    for (E e : c) {
      add(index, e);
      index++;
    }
    return false;
  }

  @Override
  public boolean removeAll(Collection<?> c) {
    boolean changed = false;
    for (Object e : c) {
      changed = remove(e) || changed;
    }
    return changed;
  }
  @Override
  public boolean removeIf(Predicate<? super E> filter) {
    boolean changed = false;
    for (E e : this) {
      if (filter.test(e)) changed = remove(e) || changed;
    }
    return changed;
  }

  @Override
  public boolean retainAll(Collection<?> c) {
    List<E> toRemove = new ArrayList<>();
    for (E e : this) {
      if (!c.contains(e)) {
        toRemove.add(e);
      }
    }
    return removeAll(toRemove);
  }

  @Override
  public void clear() {
    List<String> keys = new ArrayList<>();
    for (int i = 1; i <= size(); i++) {
      keys.add(coder.encodeKey(i));
    }
    jedis.del(keys.toArray(new String[0]));
  }

  @Override
  public E get(int index) {
    checkIndex(index);
    return coder.loadValue(jedis.get(coder.encodeKey(index)));
  }

  @Override
  public E set(int index, E element) {
    checkIndex(index);
    E prev = get(index);
    jedis.set(coder.encodeKey(index), coder.encodeValue(element));
    return prev;
  }

  @Override
  public void add(int index, E element) {
    if (index < 0 || index > size()) throw new IndexOutOfBoundsException();
    shiftRight(index);
    set(index, element);
  }

  @Override
  public E remove(int index) {
    checkIndex(index);
    E prev = get(index);
    jedis.del(coder.encodeKey(index));
    shiftLeft(index);
    return prev;
  }

  @Override
  public int indexOf(Object o) {
    for (int i = 0; i < size(); i++) {
      if (Objects.equals(o, get(i))) return i;
    }
    return -1;
  }

  @Override
  public int lastIndexOf(Object o) {
    for (int i = size() - 1; i >= 0; i--) {
      if (Objects.equals(o, get(i))) return i;
    }
    return -1;
  }

  @Override
  public void forEach(Consumer<? super E> action) {
    if (action==null) throw new NullPointerException();
    for (E e : this) action.accept(e);
  }

  @Override
  public ListIterator<E> listIterator() {
    return new RedisListIterator();
  }

  @Override
  public ListIterator<E> listIterator(int index) {
    return new RedisListIterator(index);
  }

  @Override
  public List<E> subList(int fromIndex, int toIndex) {
    if ((fromIndex < 0 || toIndex > size() || fromIndex > toIndex)) throw new UnsupportedOperationException();
    RedisList<E> subList = new RedisList<>();
    for (int i = fromIndex; i < toIndex; i++) {
      subList.add(get(i));
    }
    return subList;
  }

  private void incSize() {
    jedis.set(sizeKey, Integer.toString(size() + 1));
  }

  private void decSize() {
    jedis.set(sizeKey, Integer.toString(size() - 1));
  }

  private void checkIndex(int index) {
    if (index < 0 || index >= size()) throw new IndexOutOfBoundsException();
  }

  private class RedisIterator implements Iterator<E> {
    int current = 0;

    @Override
    public boolean hasNext() {
      return current < size();
    }

    @Override
    public E next() {
      return get(current++);
    }
  }

  private class RedisListIterator implements ListIterator<E> {
    int current;

    public RedisListIterator() {
      current = 0;
    }

    public RedisListIterator(int index) {
      current = index;
    }

    @Override
    public boolean hasNext() {
      return current <= size();
    }

    @Override
    public E next() {
      E x = get(current);
      current++;
      return x;
    }

    @Override
    public boolean hasPrevious() {
      return current > 0;
    }

    @Override
    public E previous() {
      return get(--current);
    }

    @Override
    public int nextIndex() {
      int nextIndex = current + 1;
      if (nextIndex >= size()) nextIndex = size();
      return nextIndex;
    }

    @Override
    public int previousIndex() {
      int prevIndex = current - 1;
      if (prevIndex < 0) prevIndex = -1;
      return prevIndex;
    }

    @Override
    public void remove() {
      //TODO: murtuzaaliev 03.09.2019
      throw new UnsupportedOperationException();
    }

    @Override
    public void set(E e) {
      //TODO: murtuzaaliev 03.09.2019
      throw new UnsupportedOperationException();
    }

    @Override
    public void add(E e) {
      //TODO: murtuzaaliev 03.09.2019
      throw new UnsupportedOperationException();
    }
  }

  public static void main(String[] args) {
    List<String> list = new RedisList<>();
    list.add("A");
    list.add("L");
    list.add("C");
    list.add("D");
    System.out.println(list.remove(2));
    System.out.println(Arrays.toString(list.toArray(new String[0])));
  }
}
