package eu.sshopencloud.marketplace.services.tools;

import eu.sshopencloud.marketplace.model.tools.EaseOfUse;
import eu.sshopencloud.marketplace.repositories.tools.EaseOfUseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class EaseOfUseService {

    private final EaseOfUseRepository easeOfUseRepository;

    public List<EaseOfUse> getAllEaseOfUse() {
        return easeOfUseRepository.findAll(new Sort(Sort.Direction.ASC, "ord"));
    }

}
