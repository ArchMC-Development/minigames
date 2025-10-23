package gg.tropic.practice.queue

import java.util.*
import java.util.concurrent.locks.ReentrantReadWriteLock
import kotlin.concurrent.read
import kotlin.concurrent.write

/**
 * Data class representing an entry in the queue
 */
data class InternalQueueEntry<T>(
    val id: UUID = UUID.randomUUID(),
    val data: T,
    val timestamp: Long = System.currentTimeMillis()
)

/**
 * Thread-safe queue implementation using ReentrantReadWriteLock for optimal performance.
 * Uses LinkedList for O(1) add/remove operations and HashMap for O(1) UUID lookups.
 */
class InternalQueue<T> {
    private val lock = ReentrantReadWriteLock()
    private val queue = LinkedList<InternalQueueEntry<T>>()
    private val uuidToEntry = HashMap<UUID, InternalQueueEntry<T>>()

    /**
     * Adds an entry to the end of the queue
     * @param data The data to be queued
     * @return The UUID of the created entry
     */
    fun add(data: T): UUID {
        val entry = InternalQueueEntry(data = data)
        lock.write {
            queue.addLast(entry)
            uuidToEntry[entry.id] = entry
        }
        return entry.id
    }

    /**
     * Adds a pre-created InternalQueueEntry to the end of the queue
     * @param entry The InternalQueueEntry to be queued
     * @return The UUID of the entry
     */
    fun add(entry: InternalQueueEntry<T>): UUID {
        lock.write {
            queue.addLast(entry)
            uuidToEntry[entry.id] = entry
        }
        return entry.id
    }

    /**
     * Removes and returns the first entry from the queue
     * @return The first InternalQueueEntry or null if queue is empty
     */
    fun takeFirst(): InternalQueueEntry<T>? {
        return lock.write {
            if (queue.isEmpty()) {
                null
            } else {
                val entry = queue.removeFirst()
                uuidToEntry.remove(entry.id)
                entry
            }
        }
    }

    /**
     * Removes an entry from the queue by UUID
     * @param uuid The UUID of the entry to remove
     * @return true if the entry was found and removed, false otherwise
     */
    fun remove(uuid: UUID): Boolean {
        return lock.write {
            val entry = uuidToEntry[uuid]
            if (entry != null) {
                queue.remove(entry)
                uuidToEntry.remove(uuid)
                true
            } else {
                false
            }
        }
    }

    /**
     * Removes a specific entry from the queue
     * @param entry The InternalQueueEntry to remove
     * @return true if the entry was found and removed, false otherwise
     */
    fun remove(entry: InternalQueueEntry<T>): Boolean {
        return remove(entry.id)
    }

    /**
     * Returns a copy of all elements in the queue
     * @return List of all InternalQueueEntry elements in queue order
     */
    fun listElements(): List<InternalQueueEntry<T>> {
        return lock.read {
            queue.toList()
        }
    }

    /**
     * Gets the position of an entry in the queue (0-based)
     * @param uuid The UUID of the entry
     * @return The position in the queue (0 = first), or -1 if not found
     */
    fun getPosition(uuid: UUID): Int {
        return lock.read {
            val entry = uuidToEntry[uuid]
            if (entry != null) {
                queue.indexOf(entry)
            } else {
                -1
            }
        }
    }

    /**
     * Gets the position of a specific entry in the queue (0-based)
     * @param entry The InternalQueueEntry to find
     * @return The position in the queue (0 = first), or -1 if not found
     */
    fun getPosition(entry: InternalQueueEntry<T>): Int {
        return getPosition(entry.id)
    }

    /**
     * Gets an entry by its UUID
     * @param uuid The UUID of the entry
     * @return The InternalQueueEntry or null if not found
     */
    fun getEntry(uuid: UUID): InternalQueueEntry<T>? {
        return lock.read {
            uuidToEntry[uuid]
        }
    }

    /**
     * Returns the current size of the queue
     * @return The number of elements in the queue
     */
    fun size(): Int {
        return lock.read {
            queue.size
        }
    }

    /**
     * Checks if the queue is empty
     * @return true if the queue is empty, false otherwise
     */
    fun isEmpty(): Boolean {
        return lock.read {
            queue.isEmpty()
        }
    }

    /**
     * Peeks at the first entry without removing it
     * @return The first InternalQueueEntry or null if queue is empty
     */
    fun peekFirst(): InternalQueueEntry<T>? {
        return lock.read {
            queue.firstOrNull()
        }
    }

    /**
     * Peeks at the last entry without removing it
     * @return The last InternalQueueEntry or null if queue is empty
     */
    fun peekLast(): InternalQueueEntry<T>? {
        return lock.read {
            queue.lastOrNull()
        }
    }

    /**
     * Clears all entries from the queue
     */
    fun clear() {
        lock.write {
            queue.clear()
            uuidToEntry.clear()
        }
    }

    /**
     * Checks if the queue contains an entry with the given UUID
     * @param uuid The UUID to check for
     * @return true if an entry with the UUID exists, false otherwise
     */
    fun contains(uuid: UUID): Boolean {
        return lock.read {
            uuidToEntry.containsKey(uuid)
        }
    }

    /**
     * Checks if the queue contains the specific entry
     * @param entry The InternalQueueEntry to check for
     * @return true if the entry exists, false otherwise
     */
    fun contains(entry: InternalQueueEntry<T>): Boolean {
        return contains(entry.id)
    }

    override fun toString(): String {
        return lock.read {
            "LocalQueue(size=${queue.size}, entries=${queue.map { "${it.id}:${it.data}" }})"
        }
    }
}
