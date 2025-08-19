package at.ac.uibk.dps.pulse.local.model;

import at.ac.uibk.dps.pulse.local.graph.ServiceGraph;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Represents the local state of the Pulse scheduling framework.
 * The state consists of resources and services, along with a service graph.
 */
public class State {

  private final Map<String, Resource> resources;

  private final Map<String, Service> services;

  @JsonIgnore
  private final ServiceGraph serviceGraph;

  /**
   * Constructs a new state instance.
   * @param resources the list of resources
   * @param services  the list of services
   */
  @JsonCreator
  public State(
    @JsonProperty("resources") List<Resource> resources,
    @JsonProperty("services") List<Service> services
  ) {
    this.resources = resources
      .stream()
      .collect(
        Collectors.toMap(
          Resource::getResourceName,
          Function.identity(),
          (existing, _) -> existing,
          LinkedHashMap::new
        )
      );

    this.services = services
      .stream()
      .collect(
        Collectors.toMap(
          Service::getServiceName,
          Function.identity(),
          (existing, _) -> existing,
          LinkedHashMap::new
        )
      );

    serviceGraph = ServiceGraph.builder().forServices(services).build();
  }

  /**
   * Returns the services in the state.
   * @return a list of services
   */
  public List<Service> getServices() {
    return new ArrayList<>(services.values());
  }

  /**
   * Returns the resources in the state.
   * @return a list of resources
   */
  public List<Resource> getResources() {
    return new ArrayList<>(resources.values());
  }

  /**
   * Returns the service graph for the services in the state.
   * @return the service graph
   */
  @JsonIgnore
  public ServiceGraph getServiceGraph() {
    return serviceGraph;
  }
}
