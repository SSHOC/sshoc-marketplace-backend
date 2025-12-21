package eu.sshopencloud.marketplace.services.items;

import eu.sshopencloud.marketplace.model.items.ItemExternalId;
import eu.sshopencloud.marketplace.model.items.ItemSource;
import eu.sshopencloud.marketplace.model.items.ItemStatus;
import eu.sshopencloud.marketplace.model.workflows.Workflow;
import eu.sshopencloud.marketplace.services.auth.UserService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.List;

class ItemVisibilityServiceTest {

    @Test
    void shouldGrantAccessToWorkflowWithExternalIdentifierOnHandleServerForAnonymousUser() {
        ItemVisibilityService itemVisibilityService = new ItemVisibilityService(Mockito.mock(UserService.class));
        Workflow workflow = new Workflow();
        workflow.setStatus(ItemStatus.SUGGESTED);
        ItemExternalId externalId = new ItemExternalId();
        externalId.setIdentifierService(new ItemSource("Handle", "HandleServer", 1, ""));
        workflow.setExternalIds(List.of(externalId));

        Assertions.assertTrue(itemVisibilityService.hasAccessToVersion(workflow, null));
    }

    @Test
    void shouldBlockAccessToWorkflowWithoutExternalIdentifierForAnonymousUser() {
        ItemVisibilityService itemVisibilityService = new ItemVisibilityService(Mockito.mock(UserService.class));
        Workflow workflow = new Workflow();
        workflow.setStatus(ItemStatus.SUGGESTED);

        Assertions.assertFalse(itemVisibilityService.hasAccessToVersion(workflow, null));
    }
}