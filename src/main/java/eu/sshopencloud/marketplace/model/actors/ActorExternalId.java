package eu.sshopencloud.marketplace.model.actors;

import javax.persistence.Embeddable;
import javax.persistence.Id;
import javax.persistence.ManyToOne;


@Embeddable
public class ActorExternalId {

    @Id
    @ManyToOne
    private ActorSource identifierService;

    @Id
    private String identifier;
}
