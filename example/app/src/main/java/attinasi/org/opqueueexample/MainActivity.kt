package attinasi.org.opqueueexample

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.widget.Toast
import attinasi.org.opqueue.Operation
import attinasi.org.opqueue.OperationQueue
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*


class MainActivity : AppCompatActivity(), OperationTarget {

    var pendingOperations = ArrayDeque<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    override fun onSaveInstanceState(outState: Bundle?) {
        super.onSaveInstanceState(outState)

        if (outState != null) {
            savePendingOperationIds(outState)
        }
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle?) {
        super.onRestoreInstanceState(savedInstanceState)

        if (savedInstanceState != null) {
            restoreActivityState(savedInstanceState)
        }
    }

    private fun savePendingOperationIds(outState: Bundle) {
        outState.putSerializable("OPERATIONS", pendingOperations)

        println("Stored ${pendingOperations.size} operation IDs: $pendingOperations")
    }

    private fun restoreActivityState(savedInstanceState: Bundle) {
        try {
            @Suppress("UNCHECKED_CAST")
            val pendingIds = savedInstanceState.getSerializable("OPERATIONS") as ArrayDeque<String>
            println("Loaded ${pendingIds.size} operation IDs: $pendingIds")

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

    private fun reattachToOperation(operation: Operation) {
        val demoOperation = operation as DemoOperation
        demoOperation.target = this
        pendingOperations.add(demoOperation.operationId)

        println("Reattached ${demoOperation.operationId} to new target")
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val inflater = MenuInflater(this)
        inflater.inflate(R.menu.toolbar_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {

        when (item?.itemId) {
            R.id.action_new_operation -> {
                newOperation("Luke Skywalker")
                return true
            }

            R.id.action_new_operations -> {
                for (i in 1..10) {
                    newOperation("Operation " + i)
                }
                return true
            }

            R.id.action_cancel_all -> {
                cancelAllOperations()
                return true
            }

            R.id.action_clear -> {
                clearDisplay()
                return true
            }

            R.id.action_about -> {
                showVersionInfo()
                return true
            }

            else -> {
                return super.onOptionsItemSelected(item)
            }
        }
    }

    private fun newOperation(s: String) {
        val operation = DemoOperation(s, this)

        pendingOperations.offer(operation.operationId)
        OperationQueue.instance.add(operation)

        showMessage("Operation ${operation.operationId} added to queue")
    }

    private fun cancelAllOperations() {
        while (pendingOperations.size > 0) {
            val operationId = pendingOperations.remove()
            OperationQueue.instance.cancel(operationId)
        }
    }

    private fun clearDisplay() {
        outputTextView.setText("")
    }

    private fun showVersionInfo() {
        val ver = "0.1"
        val msg = "Version: $ver"
        Toast.makeText(this, msg, Toast.LENGTH_LONG).show()
    }

    private fun showMessage(msg: String) {
        outputTextView.append("\n[${OperationQueue.instance.count()}] $msg")
    }

    override fun handleOperationResult(operationId: String, result: Operation.Result) {
        removeOperation(operationId)
        showMessage("${result.status} :  ${if (result._data != null) result._data else ""}")
    }

    private fun removeOperation(operationId: String) {
        for (id in pendingOperations) {
            if (id == operationId) {
                pendingOperations.remove(id)
                return
            }
        }
    }
}

interface OperationTarget {
    fun handleOperationResult(operationId: String, result: Operation.Result)
}

class DemoOperation(s: String, t: OperationTarget) : Operation() {
    val index = s
    var target = t

    override fun execute(): Result {
        println("DemoOperation $operationId working in background")

        val searchResult = timeConsumingOperation()

        return Result("DemoOperation $index completed", searchResult)
    }

    private fun timeConsumingOperation(): String {
        var result = "Target ${target}: "
        val limit = (Math.random() * 10.0).toLong()

        for (i in 1..limit) {
            result += i
            Thread.sleep(100)

            if (isCancelled) break
        }
        return result
    }

    override fun onComplete(result: Result) {
        println("DemoOperation $operationId Complete: ${result._data}")
        target.handleOperationResult(operationId, result)
    }

    override fun onCancel() {
        super.onCancel()

        println("DemoOperation $operationId Cancelled")
        target.handleOperationResult(operationId, Result("Operation $index Cancelled"))
    }
}
