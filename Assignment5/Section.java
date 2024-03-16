import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class Section {
    private final String name;
    private int itemCount;
    private final Lock lock = new ReentrantLock();
    private final Condition condition = lock.newCondition();
    private volatile boolean isStocking = false;

    public Section(String name, int initialItems) {
        this.name = name;
        this.itemCount = initialItems;
    }

    public void addItem(int count) {
        lock.lock();
        try {
            while (isStocking) {
                condition.await();
            }
            itemCount += count;
            condition.signalAll();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } finally {
            lock.unlock();
        }
    }

    public boolean removeItem() {
        lock.lock();
        try {
            while (itemCount == 0 || isStocking) {
                condition.await();
            }
            if (itemCount > 0) {
                itemCount--;
                condition.signalAll();
                return true;
            }
            return false;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return false;
        } finally {
            lock.unlock();
        }
    }

    public void startStocking() {
        lock.lock();
        try {
            isStocking = true;
        } finally {
            lock.unlock();
        }
    }

    public void finishStocking() {
        lock.lock();
        try {
            isStocking = false;
            condition.signalAll();
        } finally {
            lock.unlock();
        }
    }

    public boolean isStocking() {
        lock.lock();
        try {
            return isStocking;
        } finally {
            lock.unlock();
        }
    }

    public int getItemCount() {
        lock.lock();
        try {
            return itemCount;
        } finally {
            lock.unlock();
        }
    }

    // Added method
    public boolean isLowOnStock() {
        lock.lock();
        try {
            return itemCount <= 2; // Or any other threshold you define
        } finally {
            lock.unlock();
        }
    }
}
