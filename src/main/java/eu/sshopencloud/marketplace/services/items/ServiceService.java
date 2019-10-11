package eu.sshopencloud.marketplace.services.items;

import eu.sshopencloud.marketplace.model.items.Software;
import eu.sshopencloud.marketplace.model.items.Tool;
import eu.sshopencloud.marketplace.repositories.items.ServiceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ServiceService {

    private final ServiceRepository serviceRepository;

    public List<eu.sshopencloud.marketplace.model.items.Service> getAllServices() {
        return serviceRepository.findAll(new Sort(Sort.Direction.ASC, "label"));
    }

    public eu.sshopencloud.marketplace.model.items.Service getService(Long id) {
        return serviceRepository.getOne(id);
    }

}
