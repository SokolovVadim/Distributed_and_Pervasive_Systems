package it.unimi.desm.common;

public class MyLock {

    private boolean locked = false;

    public synchronized void lock() {
        while (locked) {
            try {
                wait();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                // allow upper layers to decide what to do
                throw new RuntimeException("Interrupted while waiting for lock", e);
            }
        }
        locked = true;
    }

    public synchronized void unlock() {
        locked = false;
        // wake up one waiting thread
        notify();
    }
}
