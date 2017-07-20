package attinasi.org.opqueue

import android.os.AsyncTask

internal class OperationTask(o: Operation) : AsyncTask<String, Void, Operation.Result>() {
    val operation = o

    override fun doInBackground(vararg params: String?): Operation.Result {
        val operation = OperationQueue.instance.getOperation(operation.operationId)
        if (operation != null) {
            var result = Operation.Result("Cancelled")

            if(!isCancelled) {
                result = operation.execute()
            }
            return result
        }
        return Operation.Result("Operation Not Found!")
    }

    override fun onPostExecute(result: Operation.Result?) {
        super.onPostExecute(result)

        if (result != null) {
            OperationQueue.instance.getOperation(operation.operationId)?.onComplete(result)
        }

        taskDone()
    }

    override fun onCancelled() {
        super.onCancelled()

        operation.onCancel()

        OperationQueue.instance.removeTask(operation.operationId)

        taskDone()
    }

    private fun taskDone() {
        OperationQueue.instance.scheduleNext()
    }
}