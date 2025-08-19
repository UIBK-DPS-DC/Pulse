package at.ac.uibk.dps.pulse.local.graph;

import static at.ac.uibk.dps.pulse.global.composition.PulseCompositionProblem.LATENCY_OBJECTIVE;
import static at.ac.uibk.dps.pulse.local.assignment.PulseAssignmentProblem.COST_OBJECTIVE;

import at.ac.uibk.dps.pulse.local.assignment.PulseAssignmentProblem;
import at.ac.uibk.dps.pulse.local.graph.AssignmentGraph.AssignmentEdge;
import at.ac.uibk.dps.pulse.local.graph.AssignmentGraph.AssignmentVertex;
import at.ac.uibk.dps.pulse.local.model.Resource;
import at.ac.uibk.dps.pulse.local.model.Service;
import com.google.common.collect.HashBasedTable;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeSet;
import org.apache.commons.csv.CSVFormat;
import org.jgrapht.graph.WeightedPseudograph;
import org.jgrapht.nio.AttributeType;
import org.jgrapht.nio.DefaultAttribute;
import org.jgrapht.nio.graphml.GraphMLExporter;
import org.jgrapht.nio.graphml.GraphMLExporter.AttributeCategory;
import org.moeaframework.core.Solution;
import org.moeaframework.core.variable.BinaryVariable;

/**
 * A weighted pseudograph representing the assignment of services to resources in the Pulse scheduling
 * framework.
 * This graph is used to model the relationships between services and resources, including their
 * requirements and characteristics.
 */
public class AssignmentGraph extends WeightedPseudograph<AssignmentVertex, AssignmentEdge> {

  private AssignmentGraph() {
    super(AssignmentEdge.class);
  }

  /**
   * Creates a new builder instance for constructing an assignment graph.
   * @return a new builder instance
   */
  public static AssignmentGraph.Builder builder() {
    return new AssignmentGraph.Builder();
  }

  /**
   * Converts the assignment graph to a CSV representation.
   * The CSV contains a matrix where rows represent services and columns represent resources,
   * with the values indicating the number of assignments.
   * @return a string representation of the graph in CSV format
   */
  public String toCSV() {
    try {
      final var data = HashBasedTable.<String, String, Integer>create();
      final var services = new TreeSet<String>();
      final var resources = new TreeSet<String>();

      for (final var vertex : vertexSet()) {
        if (vertex instanceof ServiceVertex sv) {
          services.add(sv.service.getServiceName());
        } else if (vertex instanceof ResourceVertex rv) {
          resources.add(rv.resource.getResourceName());
        }
      }

      for (final var edge : edgeSet()) {
        final var service = ((ServiceVertex) edge.from).service.getServiceName();
        final var resource = ((ResourceVertex) edge.to).resource.getResourceName();
        final var current = data.get(service, resource);
        data.put(service, resource, current != null ? current + 1 : 1);
      }

      var buffer = new StringBuffer();
      try (final var printer = CSVFormat.DEFAULT.print(buffer)) {
        final var header = new ArrayList<String>();
        header.add("");
        header.addAll(resources);
        printer.printRecord(header);

        for (final var service : services) {
          final var row = new ArrayList<>();
          row.add(service);
          for (final var resource : resources) {
            final var count = data.get(service, resource);
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
   * Converts the assignment graph to a GraphML representation.
   * The GraphML format is used for representing the graph structure, including vertices and edges,
   * along with their attributes.
   * @return a string representation of the graph in GraphML format
   */
  public String toGraphML() {
    final var s = new ByteArrayOutputStream();

    final var exporter = new GraphMLExporter<AssignmentVertex, AssignmentEdge>();

    exporter.registerAttribute("type", AttributeCategory.NODE, AttributeType.STRING);
    exporter.registerAttribute("label", AttributeCategory.NODE, AttributeType.STRING);
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
        case ResourceVertex resourceVertex -> {
          return Map.of(
            "type",
            DefaultAttribute.createAttribute("resource"),
            "label",
            DefaultAttribute.createAttribute(resourceVertex.resource.getResourceName()),
            "cpu",
            DefaultAttribute.createAttribute(resourceVertex.resource.getCharacteristics().getCpu()),
            "memory",
            DefaultAttribute.createAttribute(
              resourceVertex.resource.getCharacteristics().getMemory()
            ),
            "disk",
            DefaultAttribute.createAttribute(
              resourceVertex.resource.getCharacteristics().getDisk()
            ),
            "gpu",
            DefaultAttribute.createAttribute(resourceVertex.resource.getCharacteristics().getGpu()),
            "cost",
            DefaultAttribute.createAttribute(resourceVertex.resource.getCost().fixed())
          );
        }
        default -> {
          return Map.of();
        }
      }
    });

    exporter.registerAttribute("latency", AttributeCategory.EDGE, AttributeType.DOUBLE);

    exporter.setEdgeAttributeProvider(edge ->
      Map.of("latency", DefaultAttribute.createAttribute(edge.latency))
    );

    exporter.exportGraph(this, s);

    return s.toString();
  }

  /**
   * Represents a vertex in the assignment graph, which can be either a service or a resource.
   */
  public static class AssignmentVertex {}

  /**
   * Represents a vertex in the assignment graph that corresponds to a service.
   * Each service vertex contains the service it represents.
   */
  public static class ServiceVertex extends AssignmentVertex {

    private final Service service;

    public ServiceVertex(Service service) {
      this.service = service;
    }
  }

  /**
   * Represents a vertex in the assignment graph that corresponds to a resource.
   * Each resource vertex contains the resource it represents.
   */
  public static class ResourceVertex extends AssignmentVertex {

    private final Resource resource;

    public ResourceVertex(Resource resource) {
      this.resource = resource;
    }
  }

  /**
   * Represents an edge in the assignment graph, which connects a service vertex to a resource vertex.
   * Each edge has a cost and latency associated with it.
   */
  public static class AssignmentEdge {

    private final AssignmentVertex from;
    private final AssignmentVertex to;

    private final double cost;
    private final double latency;

    public AssignmentEdge(AssignmentVertex from, AssignmentVertex to, double cost, double latency) {
      this.from = from;
      this.to = to;
      this.cost = cost;
      this.latency = latency;
    }
  }

  /**
   * Builder class for constructing an assignment graph.
   * This builder allows for adding services and resources to the graph based on a given solution
   * and assignment problem.
   */
  public static class Builder {

    private final AssignmentGraph graph = new AssignmentGraph();

    private Builder() {}

    /**
     * Constructs the assignment graph for a given solution and assignment problem.
     * It adds vertices for each resource and service, and edges based on the feasible assignments
     * defined in the assignment problem.
     * @param solution the solution containing the assignments
     * @param assignmentProblem the assignment problem defining resources and services
     * @return this builder instance for method chaining
     */
    public AssignmentGraph.Builder forSolution(
      Solution solution,
      PulseAssignmentProblem assignmentProblem
    ) {
      final var resources = assignmentProblem.getResources();
      final var services = assignmentProblem.getServices();

      final var r = new HashMap<Resource, ResourceVertex>();
      final var s = new HashMap<Service, ServiceVertex>();

      resources.forEach(resource -> {
        final var v = new ResourceVertex(resource);
        graph.addVertex(v);
        r.put(resource, v);
      });
      services.forEach(service -> {
        final var v = new ServiceVertex(service);
        graph.addVertex(v);
        s.put(service, v);
      });

      final var n = services.size();

      for (int k = 0; k < n; ++k) {
        final var f = assignmentProblem.getFeasibleResources()[k];
        final var bitset = ((BinaryVariable) solution.getVariable(k)).getBitSet();
        for (int x = bitset.nextSetBit(0); x >= 0; x = bitset.nextSetBit(x + 1)) {
          final var i = f[x];

          final var resource = resources.get(i);
          final var service = services.get(k);

          final var resourceVertex = r.get(resource);
          final var serviceVertex = s.get(service);

          graph.addEdge(
            serviceVertex,
            resourceVertex,
            new AssignmentEdge(
              serviceVertex,
              resourceVertex,
              solution.getObjectiveValue(COST_OBJECTIVE),
              solution.getObjectiveValue(LATENCY_OBJECTIVE)
            )
          );
        }
      }

      return this;
    }

    /**
     * Builds the assignment graph instance.
     * @return the constructed assignment graph
     */
    public AssignmentGraph build() {
      return graph;
    }
  }
}
