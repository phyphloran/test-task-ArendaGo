package test_task_ArendaGo.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import reactor.core.scheduler.Scheduler
import reactor.core.scheduler.Schedulers
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.ThreadFactory
import java.util.concurrent.atomic.AtomicInteger

@Configuration
class BlockingExecutionConfig {

    @Bean(destroyMethod = "shutdown")
    fun blockingExecutorService(): ExecutorService =
        Executors.newFixedThreadPool(16, NamedThreadFactory("blocking-jdbc"))

    @Bean(name = ["blockingScheduler"], destroyMethod = "dispose")
    fun blockingScheduler(executorService: ExecutorService): Scheduler =
        Schedulers.fromExecutorService(executorService)
}

private class NamedThreadFactory(
    private val prefix: String
) : ThreadFactory {
    private val counter = AtomicInteger(0)

    override fun newThread(runnable: Runnable): Thread {
        val thread = Thread(runnable)
        thread.name = "$prefix-${counter.incrementAndGet()}"
        thread.isDaemon = false
        return thread
    }
}
