package eu.sshopencloud.marketplace.validators.workflows;

import eu.sshopencloud.marketplace.conf.handle.HandleServerConfiguration;
import eu.sshopencloud.marketplace.dto.items.ItemSourceCore;
import eu.sshopencloud.marketplace.model.items.ItemExternalId;
import eu.sshopencloud.marketplace.model.items.ItemSource;
import eu.sshopencloud.marketplace.model.workflows.Workflow;
import eu.sshopencloud.marketplace.services.items.ItemSourceService;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import net.handle.hdllib.AbstractMessage;
import net.handle.hdllib.AbstractRequest;
import net.handle.hdllib.AbstractResponse;
import net.handle.hdllib.Common;
import net.handle.hdllib.CreateHandleRequest;
import net.handle.hdllib.CreateHandleResponse;
import net.handle.hdllib.HandleException;
import net.handle.hdllib.HandleResolver;
import net.handle.hdllib.HandleValue;
import net.handle.hdllib.PublicKeyAuthenticationInfo;
import net.handle.hdllib.Util;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class HandleServerService {

  private final HandleServerConfiguration handleServerConfiguration;
  private final ItemSourceService itemSourceService;

  public HandleServerService(HandleServerConfiguration handleServerConfiguration, ItemSourceService itemSourceService) {
    this.handleServerConfiguration = handleServerConfiguration;
    this.itemSourceService = itemSourceService;
  }

  public void createHandleFor(Workflow workflow) throws HandleServerException {
    try {
      AbstractRequest request = createRequest(workflow);
      AbstractResponse response = sendRequest(request);

      if (requestSuccessful(response)) {
        log.debug("Successfully created handle {} for {}", new String(((CreateHandleResponse) response).handle) , workflow);
      } else {
        log.error("Unable to create handle because of {} for {}",
            AbstractMessage.getResponseCodeMessage(response.responseCode),
            workflow);
      }
    } catch (Exception e) {
      throw new HandleServerException(e);
    }
  }

  public void createExternalIdForHandleServerAndFor(Workflow workflow) {
    Optional<ItemSource> itemSource = itemSourceService.loadItemSource(getHandleServerExternalId().getCode());
    if (itemSource.isEmpty()) {
      itemSourceService.createItemSource(getHandleServerExternalId());
    } else {
      List<ItemExternalId> externalIds = workflow.getExternalIds();
      ItemExternalId externalId = new ItemExternalId(itemSource.get(), workflow.getPersistentId(), workflow);
      ArrayList<ItemExternalId> itemExternalIds =  new ArrayList<>(externalIds);
      itemExternalIds.add(externalId);
      workflow.addExternalIds(itemExternalIds);
    }
  }

  private ItemSourceCore getHandleServerExternalId() {
    return ItemSourceCore.builder()
                  .code("Handle")
                  .label("Handle")
                  .urlTemplate("https://handle.net/" + handleServerConfiguration.getAppHandleValue() + "/{source-item-id}")
                  .build();
  }
  private boolean requestSuccessful(AbstractResponse response) {
      return response.responseCode == AbstractMessage.RC_SUCCESS;
  }

  private AbstractRequest createRequest(Workflow workflow) throws Exception {
    byte[] newHandle = Util.encodeString(handleServerConfiguration.getAppHandleValue() + "/" + workflow.getPersistentId());
    HandleValue[] values = new HandleValue[]{
        new net.handle.hdllib.HandleValue(1, Common.STD_TYPE_URL,
            Util.encodeString(handleServerConfiguration.getBaseUrl() + "/workflow/" + workflow.getPersistentId()))
    };

    return new CreateHandleRequest(
        newHandle,
        values,
        authInfo()
    );
  }

  private AbstractResponse sendRequest(AbstractRequest request) throws HandleException {
    HandleResolver resolver = new HandleResolver();
    return resolver.processRequest(request);
  }

  private PublicKeyAuthenticationInfo authInfo() throws Exception {
    return new PublicKeyAuthenticationInfo(
        Util.encodeString(handleServerConfiguration.getUserIdHandle()),
        handleServerConfiguration.getUserIdIndex(),
        Util.getPrivateKeyFromFileWithPassphrase(new File(handleServerConfiguration.getKeyLocation()), "")
    );
  }
}