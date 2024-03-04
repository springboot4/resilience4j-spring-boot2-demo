minimumNumberOfCalls配置项用于指定在断路器开始考虑状态转换之前需要进行的最小调用次数。
当调用次数达到或超过此配置项指定的值时，断路器开始考虑是否应该触发状态转换（例如，从关闭状态到打开状态）。
如果在达到这个阈值之前出现了故障或异常，断路器将不会考虑状态转换。这个配置项允许在调用次数较少时避免过早地触发断路器，从而更好地适应服务的实际负载。
slidingWindowSize 表示断路器跟踪的最近调用次数，它的作用是用于计算断路器的状态。
具体来说，当我们配置了一个断路器时，它会跟踪最近一段时间内的调用情况，根据这些调用情况来判断是否需要开启或关闭断路器。
假设我们设置了 slidingWindowSize 为 10，意味着断路器将跟踪最近的 10 次调用情况。
如果在这 10 次调用中有超过 failureRateThreshold 指定的失败率的调用，那么断路器可能会触发开启状态，即进入熔断状态，不再向下游服务发起调用。
如果在开启状态下经过了 waitDurationInOpenState 指定的等待时间后，断路器会尝试转为半开启状态，即允许部分请求通过来检测服务是否恢复正常。
在半开启状态下，如果通过 permittedNumberOfCallsInHalfOpenState 指定的调用次数内有足够的成功调用，断路器将会关闭，否则会重新进入开启状态。
因此，slidingWindowSize 的值影响了断路器对调用情况的跟踪粒度，较小的值会使断路器更加敏感地感知到调用失败，而较大的值则会使其更加平滑地反应调用情况的变化。


```yaml
resilience4j.circuitbreaker: # 配置 CircuitBreaker 断路器
    configs: # 配置断路器的策略
        default: # 默认策略
            registerHealthIndicator: true # 是否注册健康指标
            slidingWindowSize: 10  # 窗口大小，表示断路器跟踪的最近调用次数
            minimumNumberOfCalls: 5  # 最小调用次数，用于计算断路器状态
            permittedNumberOfCallsInHalfOpenState: 3 # 半开状态下允许的最大调用次数
            automaticTransitionFromOpenToHalfOpenEnabled: true # 是否启用自动从开启状态转为半开启状态
            waitDurationInOpenState: 5s # 在开启状态下等待时间
            failureRateThreshold: 50 # 失败率阈值，超过此值将触发断路器开启
            eventConsumerBufferSize: 10 # 事件消费缓冲区大小
            recordExceptions: # 需要记录的异常类型列表
                - org.springframework.web.client.HttpServerErrorException
                - java.util.concurrent.TimeoutException
                - java.io.IOException
            ignoreExceptions: # 需要忽略的异常类型列表
                - io.github.robwin.exception.BusinessException
        shared: # 共享策略
            slidingWindowSize: 100 # 窗口大小
            permittedNumberOfCallsInHalfOpenState: 30 # 半开状态下允许的最大调用次数
            waitDurationInOpenState: 1s # 在开启状态下等待时间
            failureRateThreshold: 50 # 失败率阈值
            eventConsumerBufferSize: 10 # 事件消费缓冲区大小
            ignoreExceptions: # 需要忽略的异常类型列表
                - io.github.robwin.exception.BusinessException
    instances: # 实例配置
        backendA: # 后端A实例配置
            baseConfig: default # 使用默认配置
        backendB: # 后端B实例配置
            registerHealthIndicator: true # 是否注册健康指标
            slidingWindowSize: 10 # 窗口大小
            minimumNumberOfCalls: 10 # 最小调用次数
            permittedNumberOfCallsInHalfOpenState: 3 # 半开状态下允许的最大调用次数
            waitDurationInOpenState: 5s # 在开启状态下等待时间
            failureRateThreshold: 50 # 失败率阈值
            eventConsumerBufferSize: 10 # 事件消费缓冲区大小
            recordFailurePredicate: io.github.robwin.exception.RecordFailurePredicate # 记录失败的断言条件
```



`resilience4j.bulkhead` 是用于配置 Bulkhead（舱壁）模式的功能，它是一种资源隔离策略，用于保护系统免受过度负载而导致的故障。

在并发请求过多的情况下，如果不进行限制，可能会导致系统资源耗尽，从而导致系统性能下降甚至崩溃。Bulkhead 模式通过限制并发请求的数量，将系统资源划分为多个独立的区域（舱壁），以保护每个区域不受其他区域的影响。

具体来说，`resilience4j.bulkhead` 配置了以下内容：

- `maxConcurrentCalls`：最大并发调用数，表示同时允许的最大请求数，超过此数量的请求将被拒绝或等待。
- `maxWaitDuration`：最大等待时间，表示在达到最大并发调用数后，新的请求最长等待的时间，超过此时间的请求将被拒绝。

通过配置 `resilience4j.bulkhead`，我们可以在高并发情况下有效地保护系统资源，防止系统过载而导致的性能下降或崩溃。

```yaml
resilience4j.bulkhead: # 配置 Bulkhead 信号量隔离策略
    configs: # 配置 Bulkhead 信号量隔离策略的选项
        default: # 默认策略
            maxConcurrentCalls: 100 # 最大并发调用数，表示同时允许的最大请求数
    instances: # 配置 Bulkhead 信号量隔离策略的实例
        backendA: # 后端 A
            maxConcurrentCalls: 10 # 该后端的最大并发调用数
        backendB: # 后端 B
            maxWaitDuration: 10ms # 最大等待时间，表示在达到最大并发调用数后，新的请求最长等待的时间
            maxConcurrentCalls: 20 # 该后端的最大并发调用数
```

resilience4j.thread-pool-bulkhead 是用于配置线程池 Bulkhead（舱壁）策略的功能。线程池 Bulkhead 是一种资源隔离策略，它与普通的 Bulkhead 策略不同，Bulkhead 策略是基于信号量进行资源隔离，而线程池 Bulkhead 则是基于线程池进行资源隔离。

线程池 Bulkhead 的作用是限制系统中线程池的并发线程数，防止过多的任务同时进入线程池而导致系统资源耗尽、响应缓慢甚至崩溃的情况。

举例来说，假设我们有一个后端服务，处理用户请求的业务逻辑可能会涉及到一些耗时的操作，比如调用其他服务、IO 操作等。如果不加控制地将所有请求都提交给线程池处理，可能会导致线程池中的线程数量过多，从而消耗掉系统的内存和 CPU 资源，导致系统性能下降甚至崩溃。

通过配置 resilience4j.thread-pool-bulkhead，我们可以限制线程池的并发线程数，以避免系统过载。下面是一个详细的例子：

假设我们有一个后端服务，处理用户请求的业务逻辑需要调用外部服务进行数据查询和计算，并且这些外部服务可能存在响应时间不稳定的情况。我们使用线程池 Bulkhead 来保护后端服务，配置如下：

```yaml
resilience4j.thread-pool-bulkhead:
  configs:
    default:
      maxThreadPoolSize: 4 # 最大线程池大小为 4
      coreThreadPoolSize: 2 # 核心线程池大小为 2
      queueCapacity: 2 # 队列容量为 2
  instances:
    backendA:
      baseConfig: default # 使用默认配置
    backendB:
      maxThreadPoolSize: 1 # 后端 B 的最大线程池大小为 1
      coreThreadPoolSize: 1 # 后端 B 的核心线程池大小为 1
      queueCapacity: 1 # 后端 B 的队列容量为 1
```
在这个例子中，我们配置了一个默认的线程池 Bulkhead 策略，将最大线程池大小设置为 4，核心线程池大小设置为 2，并设置了队列容量为 2。然后，我们为两个后端服务分别配置了不同的线程池 Bulkhead 实例，以适应它们不同的性能需求。
通过这样的配置，我们可以有效地保护后端服务，确保它们在高负载情况下仍能提供稳定的服务，同时避免系统过载而导致的性能下降或崩溃。


假设我们有一个需要保护的后端服务，这个服务需要调用外部资源（如数据库、API 等），为了防止过多的请求导致资源耗尽或系统崩溃，我们使用 resilience4j.ratelimiter 来限制每个时间周期内允许的最大请求数。
```yarm
resilience4j.ratelimiter: # 配置限流器策略
    configs: # 配置限流器策略的选项
        default: # 默认策略
            registerHealthIndicator: false # 是否注册健康指示器，默认为 false
            limitForPeriod: 10 # 每个时间周期的限流阈值，表示每个时间周期内允许的最大请求数
            limitRefreshPeriod: 1s # 限流周期，表示限流阈值重置的时间周期
            timeoutDuration: 0 # 超时持续时间，表示请求等待执行的最长时间，单位为毫秒，0 表示无限期等待
            eventConsumerBufferSize: 100 # 事件消费者缓冲区大小，表示事件消费者队列的最大容量
    instances: # 配置限流器策略的实例
        backendA: # 后端 A
            baseConfig: default # 使用默认的配置
        backendB: # 后端 B
            limitForPeriod: 6 # 后端 B 的每个时间周期的限流阈值为 6
            limitRefreshPeriod: 500ms # 后端 B 的限流周期为 500 毫秒
            timeoutDuration: 3s # 后端 B 的超时持续时间为 3 秒

```
在这个例子中，我们配置了两个后端服务 backendA 和 backendB，它们分别有不同的限流策略。

对于 backendA，我们使用了默认配置，即每个时间周期内允许的最大请求数为 10，限流周期为 1 秒。这意味着 backendA 每秒最多只能处理 10 个请求。
对于 backendB，我们将每个时间周期内允许的最大请求数限制为 6，限流周期为 500 毫秒，超时持续时间为 3 秒。这表示 backendB 每 500 毫秒最多只能处理 6 个请求，超过这个限制的请求将会被拒绝，而且请求最长等待执行的时间为 3 秒,即请求等待执行的最长时间3s。如果请求在指定的超时时间内未能执行完成，那么该请求将被视为超时，不再等待执行，而是被快速拒绝或抛出超时异常。。
通过这样的配置，我们可以有效地保护后端服务，避免过多的请求导致资源耗尽，提高系统的稳定性和可靠性。


resilience4j.timelimiter 用于设置时间限制器，即限制方法的执行时间。它可以确保方法在指定的时间内执行完成，如果执行时间超过了设定的时间限制，将会触发超时操作，防止方法执行时间过长导致系统资源的浪费或系统性能下降。

常见的应用场景包括：

避免资源浪费： 在高并发场景下，某些方法可能会因为一些异常情况导致执行时间过长，从而占用大量系统资源，影响其他请求的处理。通过设置时间限制器，可以避免这种情况发生，及时释放资源。
保护系统性能： 一些方法可能会因为外部依赖的故障或异常情况导致执行时间过长，进而影响系统的整体性能。通过设置时间限制器，可以及时发现并处理这些异常情况，保护系统的性能不受影响。
提高系统的稳定性： 避免某些方法因为执行时间过长而导致系统崩溃或不稳定的情况发生，通过及时中断超时的方法调用，可以提高系统的稳定性和可靠性。
总的来说，resilience4j.timelimiter 通过设置方法的执行时间限制，可以帮助我们保护系统免受长时间方法调用的影响，提高系统的稳定性和可靠性。
```yaml
resilience4j.timelimiter: # 配置时间限制器策略
    configs: # 配置时间限制器策略的选项
        default: # 默认策略
            cancelRunningFuture: false # 是否取消正在运行的 Future，默认为 false，表示不取消正在运行的 Future
            timeoutDuration: 2s # 超时持续时间，表示请求的最长等待时间，单位为秒
    instances: # 配置时间限制器策略的实例
        backendA: # 后端 A
            baseConfig: default # 使用默认的配置
        backendB: # 后端 B
            baseConfig: default # 使用默认的配置
```