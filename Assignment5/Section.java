import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class Section {
    private final String name;
    private int itemCount; // num of items in the section
    private final Lock lock = new ReentrantLock(); // Control who can access what in a given section using a lock
    private final Condition condition = lock.newCondition(); //Coordination of add/remove operations and their conditional variables
    private volatile boolean isBeingStocked = false;

    // Section constructor
    public Section(String name, int initialItems) {
        this.name = name;
        this.itemCount = initialItems;
    }

    // Incorporates 'count' items into the section as it awaits restocking
    public void addItemFromSection(int count) {
        lock.lock();
        try {
            // Do not stock till; Wait
            while (isBeingStocked) {
                condition.await();
            }
            itemCount += count; // Update the item's count
            condition.signalAll(); // Notify every thread that is waiting
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt(); // Indicate that this topic should be interrupted
        } finally {
            lock.unlock();
        }
    }

    // This method waits if there are no items available or if the section is being restocked before attempting to remove an item from it.
    public boolean removeItemFromSection() {
        lock.lock();
        try {
            // Wait while there is no item or stocking.
            while (itemCount == 0 || isBeingStocked) {
                condition.await();
            }
            if (itemCount > 0) {
                itemCount--;
                condition.signalAll(); // inform all threads that are waiting
                return true;
            }
            return false;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt(); // Set the thread's stop flag
            return false;
        } finally {
            lock.unlock();
        }
    }

    // Method Starts the process of stocking
    public void startStockingProcess() {
        lock.lock();
        try {
            isBeingStocked = true;
        } finally {
            lock.unlock();
        }
    }

    // Method Ends the process of stocking
    public void finishStockingProcess() {
        lock.lock();
        try {
            isBeingStocked = false;
            condition.signalAll(); // inform all threads that are waiting
        } finally {
            lock.unlock();
        }
    }

    // Verifies the present status of section stocking
    public boolean isBeingStocked() {
        lock.lock();
        try {
            return isBeingStocked;
        } finally {
            lock.unlock();
        }
    }

    // Retrieves the current number of items in the section
    public int getItemCount() {
        lock.lock();
        try {
            return itemCount;
        } finally {
            lock.unlock();
        }
    }

    // Verifies if the section has a low stock level, using a predetermined threshold.
    public boolean isLowOnStock() {
        lock.lock();
        try {
            return itemCount <= 2; // low stock threshold
        } finally {
            lock.unlock();
        }
    }
}