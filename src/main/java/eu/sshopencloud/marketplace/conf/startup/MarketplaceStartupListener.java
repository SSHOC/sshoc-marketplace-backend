package eu.sshopencloud.marketplace.conf.startup;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class MarketplaceStartupListener {

    @EventListener( classes = { ContextRefreshedEvent.class })
    public void onApplicationRefreshedEvent(ContextRefreshedEvent event) {
        log.debug("The magic begins !");
    }

}
