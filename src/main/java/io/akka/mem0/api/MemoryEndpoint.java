package io.akka.mem0.api;

import akka.javasdk.annotations.Acl;
import akka.javasdk.annotations.http.Get;
import akka.javasdk.annotations.http.HttpEndpoint;
import akka.javasdk.annotations.http.Post;
import akka.javasdk.client.ComponentClient;
import io.akka.mem0.application.MemoryService;
import io.akka.mem0.application.MemoryStoreEntity;
import io.akka.mem0.application.SimpleExtractor;
import io.akka.mem0.domain.Memory;
import io.akka.mem0.domain.Message;
import java.util.List;

/** SPEC-001 — extraction and consolidation over one owner's memories. */
@Acl(allow = @Acl.Matcher(principal = Acl.Principal.ALL))
@HttpEndpoint("/owners/{ownerId}/memories")
public class MemoryEndpoint {

  private final ComponentClient componentClient;
  private final MemoryService service;

  public MemoryEndpoint(ComponentClient componentClient) {
    this.componentClient = componentClient;
    this.service = new MemoryService(componentClient, new SimpleExtractor());
  }

  public record AddMessages(List<Message> messages) {}

  @Post
  public List<Memory> add(String ownerId, AddMessages body) {
    return service.addMessages(ownerId, body.messages());
  }

  @Get
  public List<Memory> list(String ownerId) {
    return componentClient
        .forEventSourcedEntity(ownerId)
        .method(MemoryStoreEntity::getMemories)
        .invoke();
  }
}
