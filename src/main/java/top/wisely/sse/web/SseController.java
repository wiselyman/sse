package top.wisely.sse.web;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import top.wisely.sse.event.PayCompletedEvent;
import top.wisely.sse.listener.PayCompletedListener;

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
