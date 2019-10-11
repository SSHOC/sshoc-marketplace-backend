package eu.sshopencloud.marketplace.conf;

import eu.sshopencloud.marketplace.model.items.EasyOfUse;
import eu.sshopencloud.marketplace.repositories.items.EasyOfUseRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

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
