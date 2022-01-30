package eu.sshopencloud.marketplace.services.actors;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import eu.sshopencloud.marketplace.dto.actors.ActorHistoryDto;
import eu.sshopencloud.marketplace.mappers.actors.ActorHistoryMapper;
import eu.sshopencloud.marketplace.model.actors.Actor;
import eu.sshopencloud.marketplace.model.actors.ActorHistory;
import eu.sshopencloud.marketplace.repositories.actors.ActorHistoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class ActorHistoryService {

    private final ActorHistoryRepository actorHistoryRepository;

    public ActorHistory createActorHistory(Actor actor, Actor mergeActor){
        Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().setPrettyPrinting().create();

        String jsonHistoryString = gson.toJson(mergeActor);

        ActorHistory actorHistory = new ActorHistory();
        actorHistory.setActor(actor);
        actorHistory.setHistory(jsonHistoryString);
        actorHistory.setDateCreated(ZonedDateTime.now());

        actorHistoryRepository.save(actorHistory);

        return actorHistory;

    }


    public List<ActorHistoryDto> findHistory(Actor actor){
        List<ActorHistory> history = actorHistoryRepository.findActorHistoryByActor(actor);
        if(history==null) return new ArrayList<>();
        else return ActorHistoryMapper.INSTANCE.toDto(history);
    }
}
