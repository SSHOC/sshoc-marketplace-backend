package eu.sshopencloud.marketplace.services.tools;

import eu.sshopencloud.marketplace.model.tools.Software;
import eu.sshopencloud.marketplace.repositories.tools.SoftwareRepository;
import eu.sshopencloud.marketplace.services.items.ItemRelatedItemService;
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
