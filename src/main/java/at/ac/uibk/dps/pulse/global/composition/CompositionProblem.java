package at.ac.uibk.dps.pulse.global.composition;

import at.ac.uibk.dps.pulse.global.model.Cluster;
import at.ac.uibk.dps.pulse.local.model.Service;
import java.util.List;

/**
 * Composition problem interface provides a common interface for implementing composition problems
 * within the Pulse scheduler framework.
 */
public interface CompositionProblem {

  /**
   * Returns a flag that indicates whether the composition problem is complete, i.e., whether a full
   * composition can be made.
   * @return true if the composition problem is complete, false otherwise
   */
  boolean isComplete();

  /**
   * Returns the list of clusters that are part of the composition problem.
   * @return a list of clusters
   */
  List<Cluster> getClusters();

  /**
   * Returns the list of services that are part of the composition problem.
   * @return a list of services
   */
  List<Service> getServices();

  /**
   * Returns the mapping of service k to a cluster resource characterized by the returned index.
   * @param k the index of the service
   * @return the list of cluster resource indices for service k
   */
  List<ClusterResourceIndex> getClusterResourceIndices(int k);

  /**
   * Represents a mapping of a service to a cluster resource index.
   * @param u the index of the cluster
   * @param i the index of the resource within the cluster
   */
  record ClusterResourceIndex(int u, int i) {}
}
