package eu.sshopencloud.marketplace.conf;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class MarketplaceStartupListener {

    private final InitialDataLoader initialDataLoader;

    @EventListener
    public void onApplicationEvent(ContextRefreshedEvent event) {
        log.debug("The magic begins !");

        initialDataLoader.loadConstData();
        initialDataLoader.loadVocabularies();
        initialDataLoader.loadProfileData();
    }

}
