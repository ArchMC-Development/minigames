package gg.tropic.practice.tasks

import org.bukkit.plugin.Plugin
import org.bukkit.scheduler.BukkitRunnable
import org.bukkit.scheduler.BukkitTask

/**
 * A runnable that distributes a list of tasks evenly across multiple ticks
 * to prevent server lag from processing too many operations at once.
 */
class DistributedTaskRunnable(
    private val plugin: Plugin,
    private val tasks: List<() -> Unit>,
    private val tasksPerTick: Int = 10,
    private val onComplete: (() -> Unit)? = null
) : BukkitRunnable() {

    private var currentIndex = 0
    private var bukkitTask: BukkitTask? = null

    /**
     * Starts the distributed task execution
     */
    fun start(): BukkitTask {
        if (tasks.isEmpty()) {
            onComplete?.invoke()
            return this.runTask(plugin)
        }

        bukkitTask = this.runTaskTimer(plugin, 0L, 1L) // Run every tick
        return bukkitTask!!
    }

    override fun run() {
        // Process up to tasksPerTick tasks this tick
        var processed = 0

        while (currentIndex < tasks.size && processed < tasksPerTick) {
            try {
                tasks[currentIndex]()
            } catch (e: Exception) {
                plugin.logger.warning("Error executing task at index $currentIndex: ${e.message}")
                e.printStackTrace()
            }

            currentIndex++
            processed++
        }

        // Check if all tasks are complete
        if (currentIndex >= tasks.size) {
            finish()
        }
    }

    /**
     * Finishes the task execution and calls completion callback
     */
    private fun finish() {
        cancel()
        onComplete?.invoke()
    }

    /**
     * Gets the current progress as a percentage
     */
    fun getProgress(): Double {
        return if (tasks.isEmpty()) 100.0 else (currentIndex.toDouble() / tasks.size) * 100.0
    }

    /**
     * Gets the number of remaining tasks
     */
    fun getRemainingTasks(): Int {
        return maxOf(0, tasks.size - currentIndex)
    }

    /**
     * Force stops the execution
     */
    fun stop() {
        cancel()
    }
}

/**
 * Builder class for easier creation of DistributedTaskRunnable
 */
class DistributedTaskBuilder(private val plugin: Plugin) {
    private val tasks = mutableListOf<() -> Unit>()
    private var tasksPerTick = 10
    private var onComplete: (() -> Unit)? = null

    /**
     * Adds a single task
     */
    fun addTask(task: () -> Unit): DistributedTaskBuilder {
        tasks.add(task)
        return this
    }

    /**
     * Adds multiple tasks
     */
    fun addTasks(newTasks: Collection<() -> Unit>): DistributedTaskBuilder {
        tasks.addAll(newTasks)
        return this
    }

    /**
     * Sets how many tasks to process per tick (default: 10)
     */
    fun setTasksPerTick(count: Int): DistributedTaskBuilder {
        this.tasksPerTick = count
        return this
    }

    /**
     * Sets the completion callback
     */
    fun onComplete(callback: () -> Unit): DistributedTaskBuilder {
        this.onComplete = callback
        return this
    }

    /**
     * Builds and returns the DistributedTaskRunnable
     */
    fun build(): DistributedTaskRunnable {
        return DistributedTaskRunnable(plugin, tasks.toList(), tasksPerTick, onComplete)
    }

    /**
     * Builds and immediately starts the runnable
     */
    fun start(): DistributedTaskRunnable {
        val runnable = build()
        runnable.start()
        return runnable
    }
}

// Extension function for easier access
fun Plugin.createDistributedTask(): DistributedTaskBuilder {
    return DistributedTaskBuilder(this)
}

// Example usage:
/*
class ExamplePlugin : JavaPlugin() {

    override fun onEnable() {
        // Example 1: Using the builder
        val tasks = (1..1000).map { i ->
            {
                // Your task logic here
                logger.info("Processing task $i")
                // e.g., database operations, file I/O, etc.
            }
        }

        createDistributedTask()
            .addTasks(tasks)
            .setTasksPerTick(5) // Process 5 tasks per tick
            .onComplete {
                logger.info("All tasks completed!")
                // Additional cleanup or callback logic
            }
            .start()

        // Example 2: Manual creation
        val manualTasks = listOf<() -> Unit>(
            { logger.info("Task 1") },
            { logger.info("Task 2") },
            { logger.info("Task 3") }
        )

        val runnable = DistributedTaskRunnable(
            plugin = this,
            tasks = manualTasks,
            tasksPerTick = 1
        ) {
            logger.info("Manual tasks completed!")
        }

        runnable.start()
    }
}
*/
