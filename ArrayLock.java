import java.util.concurrent.*;
import java.util.concurrent.locks.*;
import java.util.concurrent.atomic.*;

// Array Queue Lock maintains a fixed-size array
// indicating who holds a token for threads waiting
// to enter critical section (CS).
// 
// Each thread that wants to enter CS joins at the
// end of the queue, and waits for the thread
// standing infront of it to pass it the token.
// If however it already got the token when it
// joined the queue (because it was empty), it
// enters into CS directly.
// 
// Once the thread is done with CS, it passes the
// token to the next thread behind it. If there is
// no one behind it, the token is simply placed
// there, and any later thread can simply pick up
// the token and enter CS.
// 
// As each thread only waits (spins) for its
// predecessor to hand over the token, contention
// is reduced. This also provides first-come
// first-served fairness. Due to false sharing, it
// may not be suitable for cache-coherent
// architectures. But, due to the absence of
// dynamic memory allocation, this scheme is a
// good fit for cache-less static memory
// architectures.

class ArrayLock implements Lock {
  boolean[] queue;
  int size;
  AtomicInteger tail;
  ThreadLocal<Integer> slot;
  // queue: indicates who has the token
  // size:  max allowed threads (size of queue)
  // tail:  points to end of queue
  // slot:  points where each thread stands in queue

  public ArrayLock(int capacity) {
    queue = new boolean[capacity];
    queue[0] = true;
    size = capacity;
    tail = new AtomicInteger(0);
    slot = new ThreadLocal<>() {
      protected Integer initialValue() {
        return 0;
      }
    };
  }

  // 1. When thread wants to access critical
  //    section, it stands at the end of the
  //    queue (FIFO).
  // 2. It then waits till it recieves a token from
  //    a previous thread, or if it gets the token
  //    the moment it joined the queue.
  @Override
  public void lock() {
    int s = tail.getAndIncrement() % size; // 1
    slot.set(s);                           // 1
    while(!queue[s]) Thread.yield(); // 2
  }

  // 1. When a thread is done with its critical
  //    section, it passes the token to the next
  //    thread standing in the queue.
  // 2. If there is no other thread standing next
  //    then any new thread will automatically
  //    have the token when it arrives, and thus
  //    can directly execute its CS.
  @Override
  public void unlock() {
    int s = slot.get();         // 1
    queue[s] = false;           // 1
    queue[(s+1) % size] = true; // 1, 2
  }

  @Override
  public void lockInterruptibly() throws InterruptedException {
    lock();
  }

  @Override
  public boolean tryLock() {
    lock();
    return true;
  }

  @Override
  public boolean tryLock(long arg0, TimeUnit arg1) throws InterruptedException {
    lock();
    return true;
  }

  @Override
  public Condition newCondition() {
    return null;
  }
}
