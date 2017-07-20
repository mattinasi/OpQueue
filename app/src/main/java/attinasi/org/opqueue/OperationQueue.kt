package attinasi.org.opqueue

import java.util.*

class OperationQueue {

    /**
     * access OperationQueue.instance to get the singleton OperationQueue instance
     */
    companion object {
        val instance: OperationQueue by lazy { Holder._instance }
    }

    /**
     * initialize will clear the OperationQueue and any active Operation
     */
    @Synchronized fun initialize() {
        operations.clear()
        activeTask?.cancel(true)
        activeTask = null
    }

    /**
     * add a new Operation to the end of the queue
     */
    @Synchronized fun add(operation: Operation) {
        operations.offer(OperationWrapper(operation))

        if (activeTask == null) {
            scheduleNext()
        }
    }

    /**
     * cancel will cancel a pending or currently Operation
     *   - This will cause the Operation's 'onCancel' method to be called, and will remove it from the queue
     *   - If the task being cancelled is the active task it will have to complete it's backgtound-activity before
     *     its cancellation completes.
     *
     *
     * returns false if the Operation cannot be found (presumably because it has already completed)
     * returns true if the Operation is successfully cancelled
     */
    @Synchronized fun cancel(operationId: String) : Boolean {
        val task = getTask(operationId)
        if (task != null) {
            task.cancel(false)
            return true
        }
        return false
    }

    /**
     * get the total number of operations in the queue (pending and active)
     */
    @Synchronized fun count() : Int {
        return operations.size + (if (activeTask != null) 1 else 0)
    }

    /**
     * get the total number of pending (non-active) Operations
     */
    @Synchronized fun countPending() : Int {
        return operations.size
    }

    /**
     * get an Operation if it is either active or pending
     *
     * returns null if the Operation cannot be found, presumably because it has already completed
     */
    @Synchronized fun getOperation(operationId: String) : Operation? {
        return getTask(operationId)?.operation
    }


    @Synchronized internal fun getTask(serial: String) : OperationTask? {
        if (activeTask?.operation?.operationId.equals(serial)) {
            return activeTask
        }
        val wrapper = operations.find { it.operation.operationId == serial }
        return wrapper?.task
    }

    @Synchronized internal fun removeTask(serial: String) {
        if (activeTask?.operation?.operationId.equals(serial)) {
            activeTask = null;
        } else {
            val wrapper = operations.find { it.operation.operationId == serial }
            if (wrapper != null) {
                operations.remove(wrapper)
            }
        }
    }

    @Synchronized internal fun scheduleNext() {
        val wrapper = operations.poll()
        activeTask = wrapper?.task

        activeTask?.executeOnExecutor(SerialExecutor.instance)
    }

    // singleton support
    private object Holder {
        val _instance = OperationQueue()
    }

    private val operations = ArrayDeque<OperationWrapper>()
    private var activeTask: OperationTask? = null
}
