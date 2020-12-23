package eu.sshopencloud.marketplace.services.sources;

import eu.sshopencloud.marketplace.dto.PageCoords;
import eu.sshopencloud.marketplace.dto.sources.PaginatedSources;
import eu.sshopencloud.marketplace.dto.sources.SourceCore;
import eu.sshopencloud.marketplace.dto.sources.SourceDto;
import eu.sshopencloud.marketplace.mappers.sources.SourceMapper;
import eu.sshopencloud.marketplace.model.sources.Source;
import eu.sshopencloud.marketplace.repositories.sources.SourceRepository;
import eu.sshopencloud.marketplace.validators.sources.SourceFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityNotFoundException;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class SourceService {

    private final SourceRepository sourceRepository;

    private final SourceFactory sourceFactory;


    public PaginatedSources getSources(String q, PageCoords pageCoords) {
        ExampleMatcher querySourceMatcher = ExampleMatcher.matchingAny()
                .withMatcher("label", ExampleMatcher.GenericPropertyMatchers.contains().ignoreCase())
                .withMatcher("url", ExampleMatcher.GenericPropertyMatchers.contains().ignoreCase());
        Source querySource = new Source();
        querySource.setLabel(q);
        querySource.setUrl(q);

        Page<Source> sourcesPage = sourceRepository.findAll(Example.of(querySource, querySourceMatcher),
                PageRequest.of(pageCoords.getPage() - 1, pageCoords.getPerpage(), Sort.by(Sort.Order.asc("label"))));

        List<SourceDto> sources = sourcesPage.stream().map(SourceMapper.INSTANCE::toDto).collect(Collectors.toList());

        return PaginatedSources.builder().sources(sources)
                .count(sourcesPage.getContent().size()).hits(sourcesPage.getTotalElements())
                .page(pageCoords.getPage()).perpage(pageCoords.getPerpage())
                .pages(sourcesPage.getTotalPages())
                .build();
    }

    public SourceDto getSource(Long id) {
        Source source = sourceRepository.findById(id).orElseThrow(
                () -> new EntityNotFoundException("Unable to find " + Source.class.getName() + " with id " + id));
        return SourceMapper.INSTANCE.toDto(source);
    }

    public SourceDto createSource(SourceCore sourceCore) {
        Source source = sourceFactory.create(sourceCore, null);
        source = sourceRepository.save(source);
        return SourceMapper.INSTANCE.toDto(source);
    }

    public SourceDto updateSource(Long id, SourceCore sourceCore) {
        if (!sourceRepository.existsById(id)) {
            throw new EntityNotFoundException("Unable to find " + Source.class.getName() + " with id " + id);
        }
        Source source = sourceFactory.create(sourceCore, id);
        source = sourceRepository.save(source);
        return SourceMapper.INSTANCE.toDto(source);
    }

    public void deleteSource(Long id) {
        if (!sourceRepository.existsById(id)) {
            throw new EntityNotFoundException("Unable to find " + Source.class.getName() + " with id " + id);
        }
        sourceRepository.deleteById(id);
    }

}
