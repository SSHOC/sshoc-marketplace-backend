package eu.sshopencloud.marketplace.services.items;

import eu.sshopencloud.marketplace.model.items.EasyOfUse;
import eu.sshopencloud.marketplace.repositories.items.EasyOfUseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class EasyOfUseService {

    private final EasyOfUseRepository easyOfUseRepository;

    public List<EasyOfUse> getAllEasyOfUses() {
        return easyOfUseRepository.findAll(new Sort(Sort.Direction.ASC, "ord"));
    }

}
