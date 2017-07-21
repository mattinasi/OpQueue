package attinasi.org.opqueue

import org.junit.Test

class OperationUnitTest {

    @Test
    @Throws(Exception::class)
    fun operationAssignedUniqueId() {

        val operations = ArrayList<TestOperation>()
        val count = 100

        makeOperations(operations, count)

        for (i in 0..operations.lastIndex) {
            val subject = operations[i]
            ensureSubjectIsUnique(subject, i, operations)
        }
    }

        private fun ensureSubjectIsUnique(subject: TestOperation, index: Int, operations: ArrayList<TestOperation>) {
            for (j in 0..operations.lastIndex) {
                if (index != j) {
                    assert(operations[j].operationId != subject.operationId)
                } else {
                    assert(operations[j].serial == subject.serial)
                }
            }
        }

        private fun makeOperations(operations: ArrayList<TestOperation>, count: Int) {
            (0..count).mapTo(operations) { TestOperation(it.toString()) }
        }

    @Test
    @Throws(Exception::class)
    fun operationIsAssignedId() {
        val operations = ArrayList<TestOperationWithId>()
        val count = 100

        makeOperationsWithId(operations, count)

        for (i in 0..operations.lastIndex) {
            val subject = operations[i]
            assert(subject.operationId == i.toString())
        }
    }
        private fun makeOperationsWithId(operations: ArrayList<TestOperationWithId>, count: Int) {
            (0..count).mapTo(operations) { TestOperationWithId(it.toString()) }
        }

    @Test
    @Throws(Exception::class)
    fun operationResultProperties() {
        val result = Operation.Result("SUCCESS")
        assert(result._data == null)
        assert(result.status == "SUCCESS")

        val failureObject = TestOperation("999")
        val badResult = Operation.Result("FAILURE", failureObject)
        assert(badResult._data == failureObject)
        assert(badResult.status == "FAILURE")
    }
}