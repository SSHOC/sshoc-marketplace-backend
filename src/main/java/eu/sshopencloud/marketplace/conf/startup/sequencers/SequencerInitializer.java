package eu.sshopencloud.marketplace.conf.startup.sequencers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class SequencerInitializer {

    private static final long REAL_DATA_START_SEQ = 500;

    private final List<String> TABLE_NAMES_WITH_SEQUENCERS_OR_KNOWN_IDS = Arrays.asList("users", "sources");

    private final String SEQUENCER_SUFFIX = "_id_seq";

    private final SequencerRepository sequencerRepository;


    public void initSequencers() {
        log.debug("Initializing sequencers");
        for (String tableName: TABLE_NAMES_WITH_SEQUENCERS_OR_KNOWN_IDS) {
            long max = sequencerRepository.getTableMaxId(tableName);
            log.debug("Max Id for " + tableName + ": " + max);
            if (max < REAL_DATA_START_SEQ) {
                log.debug("Initializing sequencer for " + tableName);
                sequencerRepository.setSequencerLastValue(tableName + SEQUENCER_SUFFIX, REAL_DATA_START_SEQ);
            }
        }
    }

}
