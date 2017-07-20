# OpQueue
Operation Queue for easier handling of Async Operations in Android

The key benefits of the OperationQueue are:

Static queue that is not tied to any activity's lifecycle
Activity or Fragment that launches an async operation can detach and reattach according to lifecycle events - no longer required to abandon async operations when destroyed!
Operation behaves similar to an AsyncTask by executing the long-running work on a background thread, then delivering the result to the UI thread.
The general approach is to create an Operation class that encapsulates all of the work that you want to do asynchronously, and add an instance of the Operation to the OperationQueue. If your Activity or Fragment is destroyed while the operation is pending, you can detach from it and then re-attach to it when your Activity or Fragment is re-created.

Usage looks like:

```
class DemoOperation(s: String, t: OperationTarget) : Operation() {
    val index = s
    var target = t

    override fun execute(): Result {
        println("DemoOperation $operationId working in background")

        sleep(5000) // OR, do something more interesting, like a web-service call

        return Result("DemoOperation $operationId completed search for $index", index)
    }

    override fun onComplete(result: Result) {
        println("DemoOperation $operationId Complete: ${result._data}")
        target.handleOperationResult(operationId, result)
    }

    override fun onCancel() {
        println("DemoOperation $operationId Cancelled")
        target.handleOperationResult(operationId, Result("Operation $operationId Cancelled"))
    }
}
```

Then, instantiate your Operation and add it to the singleton OperationQueue:
```
  private fun newOperation() {
      val operation = DemoOperation("Luke Skywalker", this)

      OperationQueue.instance.add(operation)
  }
```  
If your Activity or Fragment is being destroyed and re-created due to the user rotating the device, you can easily re-attach to the operation when your app is restored:
```
  override fun onCreate(savedInstanceState: Bundle?) {
      super.onCreate(savedInstanceState)
      setContentView(R.layout.activity_main)

      if (savedInstanceState != null) {
          restoreActivityState(savedInstanceState)
      } else {
          initOperationQueue()
      }
  }

  private fun restoreActivityState(savedInstanceState: Bundle) {
      restoreStatusText(savedInstanceState)

      try {
          @Suppress("UNCHECKED_CAST")
          val pendingIds = savedInstanceState.getSerializable("OPERATIONS") as ArrayDeque<String>
          reattachPendingOperations(pendingIds)
      } catch (ex: Exception) {
          pendingOperations.clear()
      }
  }

  private fun reattachPendingOperations(pendingIds: ArrayDeque<String>) {
      for (operationId in pendingIds) {
          val operation = OperationQueue.instance.getOperation(operationId)
          if (operation != null) {
              reattachToOperation(operation)
          } else {
              println("Operation $operationId not found")
          }
      }
  }
```
You can also cancel an operation if you no loner need it to be executed:
```
private fun cancelAllOperations() {
    while (pendingOperations.size > 0) {
        val operationId = pendingOperations.remove()
        OperationQueue.instance.cancel(operationId)
    }
}
```
