package at.ac.uibk.dps.pulse.global.model;

import at.ac.uibk.dps.pulse.local.model.Resource;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

/**
 * Represents a cluster in the Pulse scheduling framework.
 * A cluster consists of a name, a list of resources, and a list of candidates for each service.
 */
public class Cluster {

  private final String clusterName;

  private final List<Resource> resources;

  private final List<List<Candidate>> candidates;

  /**
   * Constructs a new cluster instance.
   * @param clusterName the name of the cluster
   * @param resources   the list of resources in the cluster
   * @param candidates  the list of candidates for each service in the cluster
   */
  @JsonCreator
  public Cluster(
    @JsonProperty("clusterName") String clusterName,
    @JsonProperty("resources") List<Resource> resources,
    @JsonProperty("candidates") List<List<Candidate>> candidates
  ) {
    this.clusterName = clusterName;
    this.resources = resources;
    this.candidates = candidates;

    assert candidates
      .stream()
      .allMatch(
        serviceCandidates -> serviceCandidates.size() == resources.size()
      ) : "Number of candidates must match number of resources for each service";
  }

  /**
   * Returns the name of the cluster.
   * @return the name of the cluster
   */
  public String getClusterName() {
    return clusterName;
  }

  /**
   * Returns the list of resources in the cluster.
   * @return the list of resources
   */
  public List<Resource> getResources() {
    return resources;
  }

  /**
   * Returns the list of candidates for each service in the cluster.
   * Each service has a list of candidates, where each candidate corresponds to a resource.
   * @return the list of candidates for each service
   */
  public List<List<Candidate>> getCandidates() {
    return candidates;
  }

  /**
   * Returns a string representation of the cluster.
   * @return a string representation of the cluster
   */
  @Override
  public String toString() {
    return String.format(
      "Cluster{clusterName=%s, resources=%s, candidates=%s}",
      getClusterName(),
      getResources(),
      getCandidates()
    );
  }
}
