package attinasi.org.opqueue

import java.util.*

/**
 * base class for all Operations - subclass this for your own purposes
 *  - NOTE: use the operationId as a unique identifier for the Operation -
 *          the ID can be assigned at construction or a unique id will be gnerated for you
 *
 *  The 'execute' method is run on a background thread, so do your long-running activity there.
 *    return an Operation.Result instance from 'execute' representing all of the results of your Operation.
 *    check for the value of isCancelled in the 'execute' method to see if the operation has been cancelled and abort further work
 *
 *  The 'onComplete' method is called on the UI thread when the 'execute' method completes.
 *    The Result of your 'execute' method will be passed in.
 *
 *  The 'onCancelled' method is called on the UI thread when an Operation has been cancelled.
 *    Be sure to call the super.onCancel method if you overrie this
 */
abstract class Operation(id: String) {

    constructor() : this(UUID.randomUUID().toString()) {}

    val operationId = id
    var isCancelled = false

    abstract fun execute() : Result
    abstract fun onComplete(result: Result)

    open fun onCancel() {
        isCancelled = true
    }
    /**
     * Operation.Result encapsulates the result of your operation's 'execute' method
     */
    class Result(s: String, data: Any? = null) {
        val status = s
        var _data = data
    }
}