package attinasi.org.opqueue

import android.os.AsyncTask
import java.util.*
import java.util.concurrent.Executor

/**
 * we implement our own serial-executor because we want to ensure that only ONE OperationTask
 * executes at a time, however we do not want to serialize across the entire process by using the
 * SERIAL_EXECUTOR constant, as that will potentially get blocked by other libraries in the app

 * This implementation is from the Android source for AsyncTask.SERIAL_EXECUTOR
 */
internal class SerialExecutor : Executor {
    internal val mTasks = ArrayDeque<Runnable>()
    internal var mActive: Runnable? = null

    @Synchronized override fun execute(r: Runnable) {
        mTasks.offer(Runnable {
            try {
                r.run()
            } finally {
                scheduleNext()
            }
        })
        if (mActive == null) {
            scheduleNext()
        }
    }

    @Synchronized protected fun scheduleNext() {
        mActive = mTasks.poll()
        if (mActive != null) {
            AsyncTask.THREAD_POOL_EXECUTOR.execute(mActive)
        }
    }

    // singleton support
    private object Holder {
        val _instance = SerialExecutor()
    }

    companion object {
        val instance: SerialExecutor by lazy { Holder._instance }
    }
}
