package com.yitu.sturnus.demo;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

public class SyncRetry {

  private final Object monitor = new Object();

  private Object connection = new Object();

  private synchronized void validateConnection(int id) {
    System.out.println("id: " + id + " enter");
    synchronized (this.monitor) {
      System.out.println("id: " + id + " try to reset connection");
      if (this.connection != null) {
        resetConnection(id);
      }
    }
  }

  private synchronized void resetConnection(int id) {
    synchronized (this.monitor) {
      if (this.connection != null) {
        this.connection = null;
        System.out.println("id: " + id + " reset the connection");
      }
    }
  }

  private synchronized void loopEnter(int id, int cnt) {
    if (cnt > 10) {
      return;
    }
    System.out.println("id: " + id + " loop cnt: " + cnt);
    loopEnter(id, cnt + 1);
  }

  public static void main(String[] args) throws InterruptedException {
    List<Thread> threads = new ArrayList<>();
    SyncRetry syncRetry = new SyncRetry();
    IntStream.range(0, 5).forEach(id -> {
      threads.add(new Thread(() -> {
        // syncRetry.validateConnection(id);
        syncRetry.loopEnter(id, 0);
      }));
    });
    for (Thread thread : threads) {
      thread.start();
    }
    for (Thread thread : threads) {
      thread.join();
    }
  }
}
