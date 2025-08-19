package at.ac.uibk.dps.pulse.global.model;

import at.ac.uibk.dps.pulse.global.graph.ClusterGraph;
import at.ac.uibk.dps.pulse.local.model.Service;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * Represents the global state of the Pulse scheduling framework.
 * The state consists of clusters, services, and a latency table between clusters.
 */
public class State {

  private final Map<String, Cluster> clusters;

  private final Map<String, Service> services;

  private final Table<String, String, Double> latency;

  @JsonIgnore
  private final ClusterGraph clusterGraph;

  /**
   * Constructs a new state instance.
   * @param clusters the list of clusters
   * @param services the list of services
   * @param latencyMap  the latency table between clusters
   */
  @JsonCreator
  public State(
    @JsonProperty("clusters") final List<Cluster> clusters,
    @JsonProperty("services") final List<Service> services,
    @JsonProperty("latency") final Map<String, Map<String, Double>> latencyMap
  ) {
    this(
      clusters,
      services,
      ((Supplier<Table<String, String, Double>>) () -> {
          final var table = HashBasedTable.<String, String, Double>create();
          latencyMap.forEach((row, colMap) ->
            colMap.forEach((col, value) -> table.put(row, col, value))
          );
          return table;
        }).get()
    );
  }

  /**
   * Constructs a new state instance.
   * @param clusters the list of clusters
   * @param services the list of services
   * @param latency the latency table between clusters
   */
  public State(
    final List<Cluster> clusters,
    final List<Service> services,
    final Table<String, String, Double> latency
  ) {
    this.clusters = clusters
      .stream()
      .collect(
        Collectors.toMap(
          Cluster::getClusterName,
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

    this.latency = latency;

    assert clusters
      .stream()
      .allMatch(
        cluster -> cluster.getCandidates().size() == services.size()
      ) : "Number of candidates must match number of services for each cluster";

    assert clusters
      .stream()
      .allMatch(cluster -> latency.columnKeySet().stream().allMatch(this.clusters::containsKey)
      ) : "All latency keys must be valid cluster names";
    assert clusters
      .stream()
      .allMatch(cluster -> latency.rowKeySet().stream().allMatch(this.clusters::containsKey)
      ) : "All latency keys must be valid cluster names";

    clusterGraph = ClusterGraph.builder().forState(this).build();
  }

  /**
   * Returns the clusters in the global state.
   * @return a list of clusters
   */
  public List<Cluster> getClusters() {
    return new ArrayList<>(clusters.values());
  }

  /**
   * Returns the services in the global state.
   * @return a list of services
   */
  public List<Service> getServices() {
    return new ArrayList<>(services.values());
  }

  /**
   * Returns the latency table between clusters.
   * @return a table of latencies between clusters, where the keys are cluster names
   */
  @JsonIgnore
  public Table<String, String, Double> getLatency() {
    return latency;
  }

  /**
   * Returns the latency table as a map.
   * The keys are row cluster names, and the values are maps of column cluster names to latencies.
   * @return a map representation of the latency table
   */
  @JsonProperty("latency")
  public Map<String, Map<String, Double>> getLatencyAsMap() {
    final var map = new HashMap<String, Map<String, Double>>();
    for (final var rowKey : latency.rowKeySet()) {
      map.put(rowKey, new HashMap<>(latency.row(rowKey)));
    }
    return map;
  }

  /**
   * Returns the cluster graph representing the global state.
   * @return the cluster graph
   */
  @JsonIgnore
  public ClusterGraph getClusterGraph() {
    return clusterGraph;
  }
}
