package redis;

import redis.clients.jedis.BinaryClient;
import redis.clients.jedis.Jedis;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

public class RedisList<E> implements List<String> {
  private Jedis jedis;
  private String listName;

  public RedisList() {
    //TODO: Добавить настройки
    jedis = new Jedis();
    listName = Long.toString(System.currentTimeMillis());
  }

  @Override
  public int size() {
    return jedis.llen(listName).intValue();
  }

  @Override
  public boolean isEmpty() {
    return jedis.llen(listName)==0;
  }

  @Override
  public boolean contains(Object o) {
    //TODO: murtuzaaliev 01.09.2019
    return false;
  }

  @Override
  public Iterator<String> iterator() {
    return jedis.lrange(listName,0,jedis.llen(listName)).iterator();
  }

  @Override
  public Object[] toArray() {
    return jedis.lrange(listName,0,jedis.llen(listName)).toArray();
  }

  @Override
  public <T> T[] toArray(T[] a) {
    return jedis.lrange(listName,0,jedis.llen(listName)).toArray(a);
  }

  @Override
  public boolean add(String s) {
    jedis.lpush(listName, s);
    return true;
  }

  @Override
  public boolean remove(Object o) {
    jedis.lrem(listName, 1, o.toString());
    return true;
  }

  @Override
  public boolean containsAll(Collection<?> c) {
    for (Object o : c) {
      if (!contains(o)) return false;
    }
    return true;
  }

  @Override
  public boolean addAll(Collection<? extends String> c) {
    return jedis.lpush(listName, c.toArray(new String[0]))!=0;
  }

  @Override
  public boolean addAll(int index, Collection<? extends String> c) {
    //TODO: murtuzaaliev 01.09.2019
    return false;
  }

  @Override
  public boolean removeAll(Collection<?> c) {
    //TODO: murtuzaaliev 01.09.2019
    return false;
  }

  @Override
  public boolean retainAll(Collection<?> c) {
    //TODO: murtuzaaliev 01.09.2019
    return false;
  }

  @Override
  public void clear() {
    jedis.del(listName);
  }

  @Override
  public String get(int index) {
    return jedis.lindex(listName,index);
  }

  @Override
  public String set(int index, String element) {
    String s = get(index);
    jedis.lset(listName,index,element);
    return s;
  }

  @Override
  public void add(int index, String element) {
    jedis.linsert(listName, BinaryClient.LIST_POSITION.BEFORE,get(index), element);
  }

  @Override
  public String remove(int index) {
    //TODO: murtuzaaliev 01.09.2019
    return null;
  }

  @Override
  public int indexOf(Object o) {
    //TODO: murtuzaaliev 01.09.2019
    return 0;
  }

  @Override
  public int lastIndexOf(Object o) {
    //TODO: murtuzaaliev 01.09.2019
    return 0;
  }

  @Override
  public ListIterator<String> listIterator() {
    //TODO: murtuzaaliev 01.09.2019 Custom Iterator
    return jedis.lrange(listName,0,jedis.llen(listName)).listIterator();
  }

  @Override
  public ListIterator<String> listIterator(int index) {
    //TODO: murtuzaaliev 01.09.2019 Custom Iterator
    return jedis.lrange(listName,index,jedis.llen(listName)).listIterator();
  }

  @Override
  public List<String> subList(int fromIndex, int toIndex) {
    return jedis.lrange(listName,fromIndex,toIndex);
  }
}
