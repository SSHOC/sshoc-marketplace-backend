package eu.sshopencloud.marketplace.services.tools;

import eu.sshopencloud.marketplace.repositories.tools.ServiceRepository;
import eu.sshopencloud.marketplace.services.items.ItemRelatedItemService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ServiceService {

    private final ServiceRepository serviceRepository;

    private final ItemRelatedItemService itemRelatedItemService;

    public List<eu.sshopencloud.marketplace.model.tools.Service> getAllServices() {
        return serviceRepository.findAll(new Sort(Sort.Direction.ASC, "label"));
    }

    public eu.sshopencloud.marketplace.model.tools.Service getService(Long id) {
        eu.sshopencloud.marketplace.model.tools.Service service = serviceRepository.getOne(id);
        service.setRelatedItems(itemRelatedItemService.getItemRelatedItems(id));
        return service;
    }

}
