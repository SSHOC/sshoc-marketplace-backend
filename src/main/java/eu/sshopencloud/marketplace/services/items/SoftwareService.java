package eu.sshopencloud.marketplace.services.items;

import eu.sshopencloud.marketplace.model.items.Software;
import eu.sshopencloud.marketplace.repositories.items.SoftwareRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SoftwareService {

    private final SoftwareRepository softwareRepository;

    private final ItemRelatedItemService itemRelatedItemService;

    public List<Software> getAllSoftware() {
        return softwareRepository.findAll(new Sort(Sort.Direction.ASC, "label"));
    }

    public Software getSoftware(Long id) {
        Software software = softwareRepository.getOne(id);
        software.setRelatedItems(itemRelatedItemService.getItemRelatedItems(id));
        return software;
    }

}
