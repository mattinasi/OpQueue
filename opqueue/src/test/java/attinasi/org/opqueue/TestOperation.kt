package attinasi.org.opqueue

class TestOperation(s: String) : Operation() {
    val serial = s

    override fun onComplete(result: Result) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onCancel() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun execute(): Result {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}

class TestOperationWithId(s: String) : Operation(s) {

    override fun execute(): Result {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onComplete(result: Result) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

}