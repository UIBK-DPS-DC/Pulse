package at.ac.uibk.dps.pulse.global.graph;

import at.ac.uibk.dps.pulse.global.graph.ClusterGraph.LatencyEdge;
import at.ac.uibk.dps.pulse.global.model.Cluster;
import at.ac.uibk.dps.pulse.global.model.State;
import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.Optional;
import org.jgrapht.graph.DirectedWeightedPseudograph;
import org.jgrapht.nio.graphml.GraphMLExporter;

/**
 * A directed weighted pseudograph representing clusters and their latencies.
 * This graph is used to model the relationships between clusters within the Pulse scheduling
 * framework.
 */
public class ClusterGraph extends DirectedWeightedPseudograph<Cluster, LatencyEdge> {

  /**
   * Constructs a new ClusterGraph instance.
   */
  public ClusterGraph() {
    super(LatencyEdge.class);
  }

  /**
   * Creates a new builder instance for constructing a ClusterGraph.
   * @return a new builder instance
   */
  public static Builder builder() {
    return new Builder();
  }

  /**
   * Retrieves the edge representing the latency between two clusters.
   * @param from the source cluster
   * @param to the target cluster
   * @return an optional latency edge; if it exists, the latency, otherwise empty
   */
  public Optional<Double> getLatency(Cluster from, Cluster to) {
    return Optional.ofNullable(getEdge(from, to)).map(LatencyEdge::latency);
  }

  /**
   * Exports the ClusterGraph to GraphML.
   * @return a string representation of the graph in GraphML format
   */
  public String toGraphML() {
    final var s = new ByteArrayOutputStream();

    final var exporter = new GraphMLExporter<Cluster, LatencyEdge>();

    exporter.exportGraph(this, s);

    return s.toString();
  }

  /**
   * A record representing an edge in the cluster graph, which includes the source and target clusters
   * and the latency between them.
   * @param from the source cluster
   * @param to the target cluster
   * @param latency the latency between the two clusters
   */
  public record LatencyEdge(Cluster from, Cluster to, double latency) {}

  /**
   * Builder class for constructing a cluster graph.
   */
  public static class Builder {

    private final ClusterGraph graph = new ClusterGraph();

    private Builder() {}

    /**
     * Constructs a ClusterGraph from a given global state.
     * @param state the global state
     * @return this builder instance
     */
    public Builder forState(State state) {
      final var vertices = new HashMap<String, Cluster>();

      state
        .getClusters()
        .forEach(cluster -> {
          vertices.put(cluster.getClusterName(), cluster);
          graph.addVertex(cluster);
        });

      for (final var cell : state.getLatency().cellSet()) {
        final var from = vertices.get(cell.getRowKey());
        final var to = vertices.get(cell.getColumnKey());
        final var value = cell.getValue();

        if (from != null && to != null) {
          final var edge = new LatencyEdge(from, to, value);
          graph.addEdge(from, to, edge);
          graph.setEdgeWeight(edge, value);
        }
      }

      return this;
    }

    /**
     * Builds the cluster graph instance.
     * @return the constructed cluster graph
     */
    public ClusterGraph build() {
      return graph;
    }
  }
}
