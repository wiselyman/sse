package top.wisely.sse.listener;

import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import top.wisely.sse.event.PayCompletedEvent;

import java.io.IOException;
import java.util.Hashtable;
import java.util.Map;

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
