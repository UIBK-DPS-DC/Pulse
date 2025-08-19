package at.ac.uibk.dps.pulse.global.graph;

import at.ac.uibk.dps.pulse.global.composition.PulseCompositionProblem;
import at.ac.uibk.dps.pulse.global.graph.CompositionGraph.CompositionEdge;
import at.ac.uibk.dps.pulse.global.graph.CompositionGraph.CompositionVertex;
import at.ac.uibk.dps.pulse.global.model.Cluster;
import at.ac.uibk.dps.pulse.local.model.Characteristics;
import at.ac.uibk.dps.pulse.local.model.Resource;
import at.ac.uibk.dps.pulse.local.model.Service;
import com.google.common.collect.HashBasedTable;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeSet;
import java.util.stream.Collectors;
import org.apache.commons.csv.CSVFormat;
import org.jgrapht.graph.DirectedWeightedMultigraph;
import org.jgrapht.nio.AttributeType;
import org.jgrapht.nio.DefaultAttribute;
import org.jgrapht.nio.graphml.GraphMLExporter;
import org.jgrapht.nio.graphml.GraphMLExporter.AttributeCategory;
import org.moeaframework.core.Solution;
import org.moeaframework.core.variable.BinaryVariable;

/**
 * The composition graph for the Pulse composition problem.
 * It represents the relationships between services and cluster resources.
 */
public class CompositionGraph
  extends DirectedWeightedMultigraph<CompositionVertex, CompositionEdge> {

  /**
   * Constructs a new empty composition graph.
   */
  public CompositionGraph() {
    super(CompositionEdge.class);
  }

  /**
   * Creates a new builder instance for constructing a composition graph.
   * @return
   */
  public static Builder builder() {
    return new Builder();
  }

  /**
   * Retrieves the fixed cost per resource in the composition graph.
   * @return a map where the keys are resources and the values are their fixed costs
   */
  public Map<Resource, Double> getFixedCostPerResource() {
    final var cost = new HashMap<Resource, Double>();
    for (final var edge : edgeSet()) {
      final var resourceVertex = (ClusterResourceVertex) edge.to;
      final var resource = resourceVertex.getResource();
      cost.put(resource, resource.getCost().fixed());
    }
    return cost;
  }

  /**
   * Calculates the utilization of each resource in the composition graph.
   * @return a map where the keys are resources and the values are their utilization ratios
   */
  public Map<Resource, Double> getUtilizationPerResource() {
    return edgeSet()
      .stream()
      .map(edge -> {
        final var service = ((ServiceVertex) edge.from).getService();
        final var resourceVertex = (ClusterResourceVertex) edge.to;
        final var resource = resourceVertex.getResource();
        final var utilization = service.getRequirements().div(resource.getCharacteristics());
        return Map.entry(resource, utilization);
      })
      .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, Characteristics::add))
      .entrySet()
      .stream()
      .collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue().max()));
  }

  /**
   * Generates a CSV representation of the composition graph.
   * @return a string containing the CSV representation of the graph
   */
  public String toCSV() {
    try {
      final var data = HashBasedTable.<String, String, Integer>create();
      final var clusters = new TreeSet<String>();
      final var resources = new TreeSet<String>();

      for (final var vertex : vertexSet()) {
        if (vertex instanceof ClusterResourceVertex rv) {
          clusters.add(rv.cluster.getClusterName());
          resources.add(rv.resource.getResourceName());
        }
      }

      for (final var edge : edgeSet()) {
        final var cluster = ((ClusterResourceVertex) edge.to).cluster.getClusterName();
        final var resource = ((ClusterResourceVertex) edge.to).resource.getResourceName();
        final var current = data.get(cluster, resource);
        data.put(cluster, resource, current != null ? current + 1 : 1);
      }

      var buffer = new StringBuffer();
      try (final var printer = CSVFormat.DEFAULT.print(buffer)) {
        final var header = new ArrayList<String>();
        header.add("");
        header.addAll(resources);
        printer.printRecord(header);

        for (final var cluster : clusters) {
          final var row = new ArrayList<>();
          row.add(cluster);
          for (final var resource : resources) {
            final var count = data.get(cluster, resource);
            row.add(count != null ? count : 0);
          }
          printer.printRecord(row);
        }
      }

      return buffer.toString();
    } catch (Exception e) {
      throw new RuntimeException("Error while writing CSV", e);
    }
  }

  /**
   * Generates a GraphML representation of the composition graph.
   * @return a string containing the GraphML representation of the graph
   */
  public String toGraphML() {
    final var s = new ByteArrayOutputStream();

    final var exporter = new GraphMLExporter<CompositionVertex, CompositionEdge>();

    exporter.registerAttribute("type", AttributeCategory.NODE, AttributeType.STRING);
    exporter.registerAttribute("label", AttributeCategory.NODE, AttributeType.STRING);
    exporter.registerAttribute("cluster", AttributeCategory.NODE, AttributeType.STRING);
    exporter.registerAttribute("resource", AttributeCategory.NODE, AttributeType.STRING);
    exporter.registerAttribute("cpu", AttributeCategory.NODE, AttributeType.DOUBLE);
    exporter.registerAttribute("memory", AttributeCategory.NODE, AttributeType.DOUBLE);
    exporter.registerAttribute("disk", AttributeCategory.NODE, AttributeType.DOUBLE);
    exporter.registerAttribute("gpu", AttributeCategory.NODE, AttributeType.DOUBLE);
    exporter.registerAttribute("cost", AttributeCategory.NODE, AttributeType.DOUBLE);

    exporter.setVertexAttributeProvider(vertex -> {
      switch (vertex) {
        case ServiceVertex serviceVertex -> {
          return Map.of(
            "type",
            DefaultAttribute.createAttribute("service"),
            "label",
            DefaultAttribute.createAttribute(serviceVertex.service.getServiceName()),
            "cpu",
            DefaultAttribute.createAttribute(serviceVertex.service.getRequirements().getCpu()),
            "memory",
            DefaultAttribute.createAttribute(serviceVertex.service.getRequirements().getMemory()),
            "disk",
            DefaultAttribute.createAttribute(serviceVertex.service.getRequirements().getDisk()),
            "gpu",
            DefaultAttribute.createAttribute(serviceVertex.service.getRequirements().getGpu())
          );
        }
        case ClusterResourceVertex clusterResourceVertex -> {
          return Map.of(
            "type",
            DefaultAttribute.createAttribute("cluster-resource"),
            "label",
            DefaultAttribute.createAttribute(clusterResourceVertex.resource.getResourceName()),
            "cluster",
            DefaultAttribute.createAttribute(clusterResourceVertex.cluster.getClusterName()),
            "resource",
            DefaultAttribute.createAttribute(clusterResourceVertex.cluster.getClusterName()),
            "cpu",
            DefaultAttribute.createAttribute(
              clusterResourceVertex.resource.getCharacteristics().getCpu()
            ),
            "memory",
            DefaultAttribute.createAttribute(
              clusterResourceVertex.resource.getCharacteristics().getMemory()
            ),
            "disk",
            DefaultAttribute.createAttribute(
              clusterResourceVertex.resource.getCharacteristics().getDisk()
            ),
            "gpu",
            DefaultAttribute.createAttribute(
              clusterResourceVertex.resource.getCharacteristics().getGpu()
            ),
            "cost",
            DefaultAttribute.createAttribute(clusterResourceVertex.resource.getCost().fixed())
          );
        }
        default -> {
          return Map.of();
        }
      }
    });

    exporter.exportGraph(this, s);

    return s.toString();
  }

  /**
   * Represents a vertex in the composition graph.
   */
  public static class CompositionVertex {}

  /**
   * Represents a vertex in the composition graph that corresponds to a service.
   */
  public static class ServiceVertex extends CompositionVertex {

    private final Service service;

    /**
     * Constructs a new service vertex with the given service.
     * @param service the service associated with this vertex
     */
    public ServiceVertex(Service service) {
      this.service = service;
    }

    /**
     * Retrieves the service associated with this vertex.
     * @return the service
     */
    public Service getService() {
      return service;
    }
  }
  /**
   * Represents a vertex in the composition graph that corresponds to a cluster resource.
   */
  public static class ClusterResourceVertex extends CompositionVertex {

    private final Cluster cluster;

    private final Resource resource;

    /**
     * Constructs a new cluster resource vertex with the given cluster and resource.
     * @param cluster the cluster associated with this vertex
     * @param resource the resource associated with this vertex
     */
    public ClusterResourceVertex(Cluster cluster, Resource resource) {
      this.cluster = cluster;
      this.resource = resource;
    }

    /**
     * Retrieves the cluster associated with this vertex.
     * @return the cluster
     */
    public Cluster getCluster() {
      return cluster;
    }

    /**
     * Retrieves the resource associated with this vertex.
     * @return the resource
     */
    public Resource getResource() {
      return resource;
    }
  }

  /**
   * Represents an edge in the composition graph that connects two vertices.
   */
  public record CompositionEdge(CompositionVertex from, CompositionVertex to) {}

  /**
   * Builder class for constructing a composition graph from a solution and a composition problem.
   */
  public static class Builder {

    private final CompositionGraph graph = new CompositionGraph();

    private Builder() {}

    /**
     * Constructs a composition graph from a given solution and composition problem.
     * @param solution the solution to be used for constructing the graph
     * @param compositionProblem the composition problem that provides the context for the graph
     * @return this builder instance
     */
    public Builder fromSolution(Solution solution, PulseCompositionProblem compositionProblem) {
      final var clusters = compositionProblem.getClusters();
      final var services = compositionProblem.getServices();

      final var r = new HashMap<Resource, ClusterResourceVertex>();
      final var s = new HashMap<Service, ServiceVertex>();

      clusters.forEach(cluster ->
        cluster
          .getResources()
          .forEach(resource -> {
            final var v = new ClusterResourceVertex(cluster, resource);
            graph.addVertex(v);
            r.put(resource, v);
          })
      );
      services.forEach(service -> {
        final var v = new ServiceVertex(service);
        graph.addVertex(v);
        s.put(service, v);
      });

      final var n = services.size();

      for (int k = 0; k < n; ++k) {
        final var bits = ((BinaryVariable) solution.getVariable(k)).getBitSet();
        for (int i = bits.nextSetBit(0); i >= 0; i = bits.nextSetBit(i + 1)) {
          final var clusterResourceIndex = compositionProblem.getClusterResourceIndices(k).get(i);

          final var resourceVertex = r.get(
            clusters.get(clusterResourceIndex.u()).getResources().get(clusterResourceIndex.i())
          );
          final var serviceVertex = s.get(services.get(k));

          graph.addEdge(
            serviceVertex,
            resourceVertex,
            new CompositionEdge(serviceVertex, resourceVertex)
          );
        }
      }

      return this;
    }

    public CompositionGraph build() {
      return graph;
    }
  }
}
