package at.ac.uibk.dps.pulse.local.graph;

import at.ac.uibk.dps.pulse.local.graph.ServiceGraph.Edge;
import at.ac.uibk.dps.pulse.local.model.Service;
import com.google.common.math.DoubleMath;
import java.io.ByteArrayOutputStream;
import java.util.Collection;
import java.util.HashMap;
import java.util.Objects;
import java.util.Optional;
import org.jgrapht.graph.DirectedWeightedMultigraph;
import org.jgrapht.nio.graphml.GraphMLExporter;

/**
 * A directed weighted multigraph representing services and their interactions.
 * This graph is used to model the relationships between services within the Pulse scheduling framework.
 */
public class ServiceGraph extends DirectedWeightedMultigraph<Service, Edge> {

  private ServiceGraph() {
    super(Edge.class);
  }

  /**
   * Creates a new builder instance for constructing a service graph.
   * @return a new builder instance
   */
  public static Builder builder() {
    return new Builder();
  }

  /**
   * Retrieves the edge representing the interaction between two services.
   * @param from the source service
   * @param to the target service
   * @return an optional edge; if it exists, the edge, otherwise empty
   */
  public Optional<Double> getDataTransfer(Service from, Service to) {
    return Optional.ofNullable(getEdge(from, to)).map(Edge::dataTransfer);
  }

  /**
   * Exports the service graph to GraphML.
   * @return a string representation of the graph in GraphML format
   */
  public String toGraphML() {
    final var s = new ByteArrayOutputStream();

    final var exporter = new GraphMLExporter<Service, Edge>();

    exporter.exportGraph(this, s);

    return s.toString();
  }

  /**
   * A record representing an edge in the service graph, which includes the source and target services,
   * the weight of the interaction, and the amount of data transferred.
   */
  public record Edge(Service from, Service to, double weight, double dataTransfer) {
    @Override
    public boolean equals(Object obj) {
      if (this == obj) {
        return true;
      }
      if (!(obj instanceof Edge(Service from1, Service v, double weight1, double transfer))) {
        return false;
      }
      return (
        from.equals(from1) &&
        to.equals(v) &&
        DoubleMath.fuzzyEquals(weight, weight1, 0.00001) &&
        DoubleMath.fuzzyEquals(dataTransfer, transfer, 0.00001)
      );
    }
  }

  /**
   * Builder class for constructing a service graph.
   */
  public static class Builder {

    private final ServiceGraph graph = new ServiceGraph();

    private Builder() {}

    /**
     * Adds services to the graph and establishes edges based on their interactions.
     * @param services a collection of services to be added to the graph
     * @return the builder instance for method chaining
     */
    public Builder forServices(Collection<Service> services) {
      final var vertices = new HashMap<String, Service>();

      services.forEach(service -> {
        vertices.put(service.getServiceName(), service);
        graph.addVertex(service);
      });

      services.forEach(from ->
        from
          .getInteractions()
          .forEach((targetServiceName, interaction) ->
            vertices
              .keySet()
              .stream()
              .filter(key -> key.equals(targetServiceName))
              .map(vertices::get)
              .filter(Objects::nonNull)
              .forEach(to -> {
                final var e = new Edge(from, to, interaction.weight(), interaction.dataTransfer());
                graph.addEdge(from, to, e);
                graph.setEdgeWeight(e, interaction.weight());
              })
          )
      );

      return this;
    }

    /**
     * Builds the service graph instance.
     * @return the constructed service graph
     */
    public ServiceGraph build() {
      return graph;
    }
  }
}
