package com.ericsson.ldap.test;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.context.request.async.DeferredResult;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.io.IOException;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.time.LocalTime;
import java.util.Date;
import java.util.Random;
import java.util.concurrent.*;

@Controller
public class HelloController {
    private Logger logger = LoggerFactory.getLogger(this.getClass());
    @RequestMapping("/async/test")
    public Callable<String> callable() {
        return new Callable<String>() {
            @Override
            public String call() throws Exception {
                //....
                return System.currentTimeMillis()+"";
            }
        };
    }

    @RequestMapping("callableTest")
    public @ResponseBody Callable<String> handleTestRequest () {
        logger.info("handler started");
        Callable<String> callable = new Callable<String>() {
            @Override
            public String call () throws Exception {
                logger.info("async task started");
                Thread.sleep(2000);
                logger.info("async task finished");
                return "async result";
            }
        };

        logger.info("handler finished");
        return callable;
    }

    @GetMapping("/download")
    public StreamingResponseBody handle() {
        return new StreamingResponseBody() {
            @Override
            public void writeTo(OutputStream outputStream) throws IOException {
                // write...
            }
        };
    }

    @RequestMapping("/testCallable")
    public Callable<String> testCallable() {
        logger.info("Controller开始执行！");
        Callable<String> callable = () -> {
            Thread.sleep(5000);
            logger.info("实际工作执行完成！");
            return "succeed!";
        };
        logger.info("Controller执行结束！");
        return callable;
    }

    private LongTimeAsyncCallService longTimeAsyncCallService;

    @RequestMapping(value="/asynctask", method = RequestMethod.GET)
    public DeferredResult<ModelAndView> asyncTask(){
        DeferredResult<ModelAndView> deferredResult = new DeferredResult<ModelAndView>();
        System.out.println("/asynctask 调用！thread id is : " + Thread.currentThread().getId());
        //模拟长时间异步调用的服务类
        longTimeAsyncCallService.makeRemoteCallAndUnknownWhenFinish(new LongTermTaskCallback() {
            @Override
            public void callback(Object result) {
                System.out.println("异步调用执行完成, thread id is : " + Thread.currentThread().getId());
                ModelAndView mav = new ModelAndView("remotecalltask");
                mav.addObject("result", result);
                deferredResult.setResult(mav);
            }
        });
        return deferredResult;
    }

    private ExecutorService nonBlockingService = Executors.newCachedThreadPool();
    @GetMapping("/sse")
    public SseEmitter handleSse() {
        SseEmitter emitter = new SseEmitter();

        nonBlockingService.execute(() -> {
            for (int i = 0; i < 500; i++) {
                try {
                    emitter.send("/sse" + " @ " + new Date());
                    Thread.sleep(200);
                } catch (Exception e) {
                    e.printStackTrace();
                    emitter.completeWithError(e);
                    return;
                }
            }
            emitter.complete();
        });
        return emitter;
    }

    @RequestMapping("/sseTest")
    public SseEmitter handleRequest () {

        final SseEmitter emitter = new SseEmitter();
        ExecutorService service = Executors.newSingleThreadExecutor();
        service.execute(() -> {
            for (int i = 0; i < 500; i++) {
                try {
                    emitter.send(LocalTime.now().toString() , MediaType.TEXT_PLAIN);
                    Thread.sleep(200);
                } catch (Exception e) {
                    e.printStackTrace();
                    emitter.completeWithError(e);
                    return;
                }
            }
            emitter.complete();
        });

        return emitter;
    }

    @RequestMapping("/test2")
    public SseEmitter handleRequest2() {

        final SseEmitter emitter = new SseEmitter();
        ExecutorService service = Executors.newSingleThreadExecutor();
        service.execute(() -> {
            for (int i = 0; i < 1000; i++) {
                try {
                    //an HttpMessageConverter will convert BigDecimal in proper format
                    emitter.send(new BigDecimal(i));
                    emitter.send(" - ", MediaType.TEXT_PLAIN);

                    Thread.sleep(10);
                } catch (Exception e) {
                    e.printStackTrace();
                    emitter.completeWithError(e);
                    return;
                }
            }
            emitter.complete();
        });

        return emitter;
    }
}
interface LongTermTaskCallback {
    void callback(Object result);
}
class LongTimeAsyncCallService {
    private final int CorePoolSize = 4;
    private final int NeedSeconds = 3;
    private Random random = new Random();
    private ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(CorePoolSize);
    public void makeRemoteCallAndUnknownWhenFinish(LongTermTaskCallback callback){
        System.out.println("完成此任务需要 : " + NeedSeconds + " 秒");
        scheduler.schedule(new Runnable() {
            @Override
            public void run() {
                callback.callback("长时间异步调用完成.");
            }
        }, 1000L, TimeUnit.SECONDS);
    }
}
