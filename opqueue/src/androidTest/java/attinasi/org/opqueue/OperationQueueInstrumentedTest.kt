package attinasi.org.opqueue

import android.support.test.runner.AndroidJUnit4
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.util.*

class GatedOperation() : Operation() {
    var canContinue = false
    var completed = false

    fun allowContinuation() {
        canContinue = true
    }

    override fun execute(): Result {
        while (!canContinue) {
            Thread.sleep(10)

            if (isCancelled) break
        }
        return Result("OK")
    }

    override fun onComplete(result: Result) {
        println("GatedOperation::onComplete")
        completed = true
    }

    override fun onCancel() {
        super.onCancel()
        println("GatedOperation::onCancel")
    }
}

class SequentialOperation(n: String, results: ArrayList<String>) : Operation() {
    val _results = results
    val name = n

    override fun execute(): Result {
        Thread.sleep(100)

        return Result(name)
    }

    override fun onComplete(result: Result) {
        _results.add(result.status)
    }

    override fun onCancel() {
        super.onCancel()
    }
}

@RunWith(AndroidJUnit4::class)
class OperationQueueInstrumentedTest {

    @Before
    fun setup() {
        OperationQueue.instance.initialize()
    }

    @Test
    @Throws(Exception::class)
    fun operationQueueInitialState() {
        val instance = OperationQueue.instance
        assert(instance.count() == 0)
    }

    @Test
    @Throws(Exception::class)
    fun operationQueueSingleInstance() {
        val i1 = OperationQueue.instance
        val i2 = OperationQueue.instance
        assert(i1 == i2)
    }

    @Test
    @Throws(Exception::class)
    fun addOperations() {
        val gatedOperation = GatedOperation()
        OperationQueue.instance.add(gatedOperation)
        assertEquals(1, OperationQueue.instance.count())
        assertEquals(0, OperationQueue.instance.countPending())

        for (i in 1..100) {
            OperationQueue.instance.add(GatedOperation())
        }
        assertEquals(100, OperationQueue.instance.countPending())
        assertEquals(101, OperationQueue.instance.count())
    }

    @Test
    @Throws(Exception::class)
    fun executeOperationsSequentially() {

        val operations = ArrayList<SequentialOperation>()
        val results = ArrayList<String>()

        // create the operations, passing the result-array
        for (i in 0..25) {
            operations.add(SequentialOperation(i.toString(), results))
        }

        // add them to the queue
        for (op in operations) {
            OperationQueue.instance.add(op)
        }

        // wait for them to finish
        val startTime = Date()
        while (OperationQueue.instance.count() > 0) {
            Thread.sleep(10)

            if (Date().time - startTime.time > 5000) {
                assertTrue("Too much time waiting!", false )
            }
        }

        // make sure they executed in the correct order by looking at the result-array
        for (i in 0..25) {
            assertEquals(results[i], i.toString())
        }
    }

    @Test
    @Throws(Exception::class)
    fun cancelOperations() {
        val gatedOperation = GatedOperation()
        val operationQueue = OperationQueue.instance
        operationQueue.add(gatedOperation)
        assertEquals(1, operationQueue.count())
        assertEquals(0, operationQueue.countPending())

        operationQueue.cancel(gatedOperation.operationId)
        Thread.sleep(100)
        assertEquals(0, operationQueue.count())

        val operations = ArrayList<GatedOperation>()

        for (i in 1..11) {
            val op = GatedOperation()
            operationQueue.add(op)
            operations.add(op)
        }
        assertEquals(11, operationQueue.count())
        assertEquals(10, operationQueue.countPending())

        for (op in operations) {
            val id = op.operationId
            assertTrue(operationQueue.cancel(id))
        }

        Thread.sleep(100)
        assertEquals(0, operationQueue.count())
    }
}

