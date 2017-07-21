package attinasi.org.opqueue

internal class OperationWrapper(op: Operation) {
    val operation = op
    val task = OperationTask(op)
}