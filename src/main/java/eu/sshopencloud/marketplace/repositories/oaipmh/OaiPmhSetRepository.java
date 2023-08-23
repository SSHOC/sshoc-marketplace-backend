package eu.sshopencloud.marketplace.repositories.oaipmh;

import io.gdcc.xoai.dataprovider.model.Set;
import io.gdcc.xoai.dataprovider.repository.SetRepository;
import org.springframework.stereotype.Service;

import java.util.List;
@Service
public class OaiPmhSetRepository implements SetRepository {
    @Override
    public List<Set> getSets() {
        return List.of();
    }
}
