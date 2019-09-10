package redis;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.Pipeline;

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
  private List<Integer> deleted = new ArrayList<>();
  String listName;

  public RedisList() {
    //TODO: Добавить настройки
    jedis = new Jedis();
    listName = Long.toString(System.currentTimeMillis());
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
    return !jedis.lrange(coder.encodeObject(o),0,0).isEmpty();
  }

  @Override
  public Iterator<E> iterator() {
    return new RedisIterator();
  }

  @Override
  public Object[] toArray() {
    Object[] res = new Object[size()];
    int k = 0;
    for (int i = 0; i < size() + deleted.size(); i++)
      if (!deleted.contains(i)) {
        res[k] = get(i);
        k++;
      }
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
    int key = size() + deleted.size();
    jedis.set(coder.encodeKey(key), coder.encodeValue(s));
    String reverseIndex = coder.encodeObject(s);
    jedis.rpush(reverseIndex, Integer.toString(key));
    incSize();
    return true;
  }

  @Override
  public boolean remove(Object o) {
    String encodedObject = jedis.lpop(coder.encodeObject(o));
    if (encodedObject != null) {
      Integer indexToDelete = Integer.valueOf(encodedObject);
      remove(indexToDelete.intValue());
      return true;
    }
    return false;
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
    Pipeline p = jedis.pipelined();
    for (Object o : c) {
      p.lrange(coder.encodeObject(o),0,0);
    }
    return p.syncAndReturnAll().stream().map(x -> ((List) x).size()).noneMatch(x -> x == 0);
  }

  @Override
  public boolean addAll(Collection<? extends E> c) {
    Pipeline p = jedis.pipelined();
    int key = size() + deleted.size();
    int inkSize=0;
    for (E e : c) {
      p.set(coder.encodeKey(key), coder.encodeValue(e));
      p.rpush(coder.encodeObject(e), Integer.toString(key));
      inkSize++;
      key++;
    }
    p.sync();
    incSize(inkSize);
    return true;
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
    List<String> toDelete = new ArrayList<>();
    for (Object e : c) {
      String encodedObject = jedis.lpop(coder.encodeObject(e));
      if (encodedObject != null) {
        int i = Integer.parseInt(encodedObject);
        deleted.add(i);
        toDelete.add(coder.encodeKey(i));
      }
    }
    jedis.del(toDelete.toArray(new String[0]));
    decSize(toDelete.size());
    return !toDelete.isEmpty();
  }

  @Override
  public boolean removeIf(Predicate<? super E> filter) {
    if (filter == null) throw new NullPointerException();
    List<String> toDelete = new ArrayList<>();
    for (E e : this) {
      if (filter.test(e)) {
        String encodedObject = jedis.lpop(coder.encodeObject(e));
        if (encodedObject != null) {
          int i = Integer.parseInt(encodedObject);
          deleted.add(i);
          toDelete.add(coder.encodeKey(i));
        }
      }
    }
    jedis.del(toDelete.toArray(new String[0]));
    decSize(toDelete.size());
    return !toDelete.isEmpty();
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
    for (int i = 0; i <= size(); i++) {
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
    jedis.lrem(coder.encodeObject(prev),1,Integer.toString(index));
    jedis.set(coder.encodeKey(index), coder.encodeValue(element));
    jedis.set(coder.encodeObject(element), Integer.toString(index));
    return prev;
  }

  @Override
  public void add(int index, E element) {
    //TODO: Продумать вставку в середину
    if (index < 0 || index > size()) throw new IndexOutOfBoundsException();
    shiftRight(index);
    set(index, element);
  }

  @Override
  public E remove(int index) {
    checkIndex(index);
    E prev = get(index);
    deleted.add(index);
    jedis.del(coder.encodeKey(index));
    decSize();
    return prev;
  }

  @Override
  public int indexOf(Object o) {
    String index = jedis.get(coder.encodeObject(o));
    if (index != null) return Integer.parseInt(index);
    return -1;
  }

  //TODO: Продумать ситуацию когда несколько одинаковых элемента в списке
  @Override
  public int lastIndexOf(Object o) {
    for (int i = size() - 1; i >= 0; i--) {
      if (Objects.equals(o, get(i))) return i;
    }
    return -1;
  }

  @Override
  public void forEach(Consumer<? super E> action) {
    if (action == null) throw new NullPointerException();
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

  private void incSize(int inkSize) {
    jedis.set(sizeKey, Integer.toString(size() + inkSize));
  }


  private void decSize() {
    jedis.set(sizeKey, Integer.toString(size() - 1));
  }

  private void decSize(int x) {
    jedis.set(sizeKey, Integer.toString(size() - x));
  }

  private void checkIndex(int index) {
    if (index < 0 || index - deleted.size() >= size()) throw new IndexOutOfBoundsException();
  }

  private class RedisIterator implements Iterator<E> {
    protected Iterator<E> data;
    int current = 0;

    public RedisIterator() {
      List<E> data = new ArrayList<>();
      for (int i = 0; i < size() + deleted.size(); i++) {
        if (!deleted.contains(i)) {
          data.add(get(i));
        }
      }
      this.data = data.iterator();
    }

    @Override
    public boolean hasNext() {
      return data.hasNext();
    }

    @Override
    public E next() {
//      while (deleted.contains(current)) current++;
//      return get(current++);
      return data.next();
    }
  }

  private class RedisListIterator extends RedisIterator implements ListIterator<E> {
    //TODO: Implement List Iterator
    int current;
    ListIterator<E> listIterator;

    public RedisListIterator() {
      super();
      List<E> copy = new ArrayList<>();
      while (data.hasNext())
        copy.add(data.next());

      listIterator = copy.listIterator();
    }

    public RedisListIterator(int index) {
      super();
      List<E> copy = new ArrayList<>();
      while (data.hasNext())
        copy.add(data.next());

      listIterator = copy.subList(index, copy.size()).listIterator();
    }

    @Override
    public boolean hasNext() {
//      while (deleted.contains(current)) current++;
//      return current < size() + deleted.size();
      return listIterator.hasNext();
    }

    @Override
    public E next() {
//      E x = get(current);
//      current++;
//      return x;
      return listIterator.next();
    }

    @Override
    public boolean hasPrevious() {
//      while (deleted.contains(current)) current--;
//      return current > 0;
      return listIterator.hasPrevious();
    }

    @Override
    public E previous() {
//      return get(current--);
      return listIterator.previous();
    }

    @Override
    public int nextIndex() {
//      int nextIndex = current + 1;
//      if (nextIndex >= size()) nextIndex = size();
//      return nextIndex;
      return listIterator.nextIndex();
    }

    @Override
    public int previousIndex() {
//      int prevIndex = current - 1;
//      if (prevIndex < 0) prevIndex = -1;
//      return prevIndex;
      return listIterator.previousIndex();
    }

    @Override
    public void remove() {
      //TODO: murtuzaaliev 03.09.2019
      listIterator.remove();
    }

    @Override
    public void set(E e) {
      //TODO: murtuzaaliev 03.09.2019
      listIterator.set(e);
    }

    @Override
    public void add(E e) {
      //TODO: murtuzaaliev 03.09.2019
      listIterator.add(e);
    }
  }
}
