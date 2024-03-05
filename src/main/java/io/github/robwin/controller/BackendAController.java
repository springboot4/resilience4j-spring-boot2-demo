package io.github.robwin.controller;

import io.github.robwin.service.Service;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping(value = "/backendA")
public class BackendAController {

    private final Service businessAService;

    public BackendAController(@Qualifier("backendAService") Service businessAService){
        this.businessAService = businessAService;
    }

    /**
     * <p>断路器</p>
     * <p>
     * 断路器以10次调用为周期计算失败率。一旦调用次数达到5次，开始计算失败率，一旦失败率超过50%，断路器将会打开，
     * 后续的请求会直接返回fallback结果。在断路器打开的状态下，所有的请求都会被快速失败，
     * 不会再尝试调用被保护的方法。
     * </p>
     *
     * <p>经过等待时间（5秒）后，断路器会进入半开启状态。在半开启状态下，断路器将允许一定数量（3个）的请求通过，
     * 以检测被保护的方法是否已经恢复正常。如果这些请求中的成功率超过50%，则断路器会关闭，
     * 允许后续的请求正常调用被保护的方法。如果仍然失败，则断路器会继续保持打开状态。
     * </p>
     *
     * <p>限流</p>
     * <p>
     * 限流策略设置了最大并发数为10。这意味着超过10个并发请求会被限制，并且直接返回fallback结果。
     * </p>
     *
     * <p>重试</p>
     * <p>
     * 重试策略设置了最多重试3次，并且每次重试之间的间隔为100毫秒。
     * </p>
     */
    @GetMapping("failure")
    public String failure(){
        return businessAService.failure();
    }

    /**
     * 调用成功 不会重试
     */
    @GetMapping("success")
    public String success(){
        return businessAService.success();
    }

    /**
     * 没有记录的异常 不会打开断路器
     */
    @GetMapping("successException")
    public String successException(){
        return businessAService.successException();
    }

    /**
     * 因为抛出的是被忽略的异常 所以不会打开断路器
     */
    @GetMapping("ignore")
    public String ignore(){
        return businessAService.ignoreException();
    }

    /**
     * 调用成功
     */
    @GetMapping("monoSuccess")
    public Mono<String> monoSuccess(){
        return businessAService.monoSuccess();
    }

    /**
     * 调用失败，从minimumNumberOfCalls(5)次开始计算失败率，超过failureRateThreshold(50%)后打开断路器 此时也会触发重试
     */
    @GetMapping("monoFailure")
    public Mono<String> monoFailure(){
        return businessAService.monoFailure();
    }

    /**
     * 调用成功
     */
    @GetMapping("fluxSuccess")
    public Flux<String> fluxSuccess(){
        return businessAService.fluxSuccess();
    }

    /**
     * 每次调用都会超时失败，从minimumNumberOfCalls(5)次开始计算失败率，超过failureRateThreshold(50%)后打开断路器，每次返回monoFallback
     */
    @GetMapping("monoTimeout")
    public Mono<String> monoTimeout(){
        return businessAService.monoTimeout();
    }

    /**
     * 每次调用都会超时失败，从minimumNumberOfCalls(5)次开始计算失败率，超过failureRateThreshold(50%)后打开断路器，每次返回fluxFallback
     */
    @GetMapping("fluxTimeout")
    public Flux<String> fluxTimeout(){
        return businessAService.fluxTimeout();
    }

    /**
     * 线程池隔离,超出线程池容量的失败
     */
    @GetMapping("futureFailure")
    public CompletableFuture<String> futureFailure(){
        return businessAService.futureFailure();
    }

    /**
     * 线程池隔离，超出线程池容量调用失败
     */
    @GetMapping("futureSuccess")
    public CompletableFuture<String> futureSuccess(){
        return businessAService.futureSuccess();
    }

    /**
     * 线程池隔离 注意返回的fallback内容 10个里面是6个超时 4个线程池满
     */
    @GetMapping("futureTimeout")
    public CompletableFuture<String> futureTimeout(){
        return businessAService.futureTimeout();
    }

    /**
     * 失败+重试+断路
     */
    @GetMapping("fluxFailure")
    public Flux<String> fluxFailure(){
        return businessAService.fluxFailure();
    }

    /**
     * 失败+fallback但是不重试
     */
    @GetMapping("fallback")
    public String failureWithFallback(){
        return businessAService.failureWithFallback();
    }
}
