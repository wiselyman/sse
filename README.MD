# 使用SSE监听异步通知并推送到客户端(Spring Boot 2.0 + SseEmitter)

## 1.场景

当我们做异步请求处理的时候，客户端向服务端的请求不是立即返回的，而是在服务端有数据的时候才发送给客户端的。本例演示是服务端数据是另外的事件触发并推送给客户端的。

当使用微信或者支付宝扫码支付的时候，在客户支付成功之后，后台得到微信(支付宝)回调需要通知我们的客户端支付已经完成，客户端得到通知后可以做一些后续的操作。
这涉及到后台需要将支付成功的消息主动推送给客户端，让客户端记性后续的操作。

- 客户端向后台(`SseController`->`push`)发送异步请求，客户端处于监听等待状态;
- 微信(支付宝)支付成功后回调后台(`SseController`->`payCallback`模拟);
- `payCallback`方法通过`applicationContext.publishEvent`向系统内部发送支付完成事件;
- `push`方法通过`payCompletedListener`监听事件并通过`SseEmitter`发送给客户端。

名词解释：`SSE`-`server send event`是一种服务端推送的技术，本例使用`SseEmitter`来实现。



## 2.`SseController`

```java
@RestController
public class SseController {

    @Autowired
    ApplicationContext applicationContext;
    @Autowired
    PayCompletedListener payCompletedListener;

    @GetMapping("/push")
    public SseEmitter push(@RequestParam Long payRecordId){
        final SseEmitter emitter = new SseEmitter();
        try {
           payCompletedListener.addSseEmitters(payRecordId,emitter);
        }catch (Exception e){
            emitter.completeWithError(e);
        }

        return emitter;
    }

    @GetMapping("/pay-callback")
    public String payCallback(@RequestParam Long payRecordId){
        applicationContext.publishEvent(new PayCompletedEvent(this,payRecordId));
        return "请到监听处查看消息";

    }

}

```

## 3.`PayCompletedEvent`

```java
@Getter
@Setter
public class PayCompletedEvent extends ApplicationEvent {
    private Long payRecordId;

    public PayCompletedEvent(Object source, Long payRecordId) {
        super(source);
        this.payRecordId = payRecordId;
    }
}

```


## 4.PayCompletedListener

```java
@Component
public class PayCompletedListener {
    private static Map<Long,SseEmitter> sseEmitters = new Hashtable<>();

    public void addSseEmitters(Long payRecordId, SseEmitter sseEmitter) {
        sseEmitters.put(payRecordId, sseEmitter);
    }

    @EventListener
    public void deployEventHandler(PayCompletedEvent payCompletedEvent) throws IOException {
        Long payRecordId = payCompletedEvent.getPayRecordId();
        SseEmitter sseEmitter = sseEmitters.get(payRecordId);
        sseEmitter.send("支付成功");
        sseEmitter.complete();

    }
}

```

## 5.演示

- 先访问`http://localhost:8080/push?payRecordId=123`,此时客户端处于等待状态.

![](https://raw.githubusercontent.com/wiselyman/sse/master/images/sse1.png)

- 再访问`http://localhost:8080/pay-callback?payRecordId=123`,模拟回调通知客户端支付成功,这是会通知上一步并推送到客户端.
![](https://raw.githubusercontent.com/wiselyman/sse/master/images/sse2.png)
