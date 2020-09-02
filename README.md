Array Queue Lock maintains a fixed-size array
indicating who holds a token for threads waiting
to enter critical section (CS).

Each thread that wants to enter CS joins at the
end of the queue, and waits for the thread
standing infront of it to pass it the token.
If however it already got the token when it
joined the queue (because it was empty), it
enters into CS directly.

Once the thread is done with CS, it passes the
token to the next thread behind it. If there is
no one behind it, the token is simply placed
there, and any later thread can simply pick up
the token and enter CS.

As each thread only waits (spins) for its
predecessor to hand over the token, contention
is reduced. This also provides first-come
first-served fairness. Due to false sharing, it
may not be suitable for cache-coherent
architectures. But, due to the absence of
dynamic memory allocation, this scheme is a
good fit for cache-less static memory
architectures.

[Tom Anderson] invented the ALock algorithm and was
one of the first to empirically study the 
performance of spin locks in shared memory
multiprocessors.

[Tom Anderson]: https://scholar.google.com/citations?user=MYqlcPgAAAAJ&hl=en

```java
1. When thread wants to access critical
   section, it stands at the end of the
   queue (FIFO).
2. It then waits till it recieves a token from
   a previous thread, or if it gets the token
   the moment it joined the queue.
```

```java
1. When a thread is done with its critical
   section, it passes the token to the next
   thread standing in the queue.
2. If there is no other thread standing next
   then any new thread will automatically
   have the token when it arrives, and thus
   can directly execute its CS.
```

See [ArrayLock.java] for code, [Main.java] for test, and [repl.it] for output.

[ArrayLock.java]: https://repl.it/@wolfram77/array-lock#ArrayLock.java
[Main.java]: https://repl.it/@wolfram77/array-lock#Main.java
[repl.it]: https://array-lock.wolfram77.repl.run


### references

- [The Art of Multiprocessor Programming :: Maurice Herlihy, Nir Shavit](https://dl.acm.org/doi/book/10.5555/2385452)
- [The performance of spin lock alternatives for shared-memory multiprocessors :: Thomas Anderson](https://ieeexplore.ieee.org/document/80120)
