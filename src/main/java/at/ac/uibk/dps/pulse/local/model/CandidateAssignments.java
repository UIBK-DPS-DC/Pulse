package at.ac.uibk.dps.pulse.local.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.moeaframework.core.Solution;
import org.moeaframework.core.variable.BinaryVariable;

/**
 * Represents the candidate assignments for resources in a Pulse scheduling solution.
 * This class encapsulates the mapping of resources to their candidate services and the associated cost.
 */
public class CandidateAssignments {

  private final Map<Integer, List<Integer>> resourceCandidates;

  private final double cost;

  /**
   * Constructs a new CandidateAssignments instance from a given solution.
   * The solution is expected to contain binary variables representing the assignment of services to resources.
   * @param solution the solution containing the assignments
   */
  public CandidateAssignments(Solution solution) {
    resourceCandidates = new HashMap<>();

    for (int i = 0; i < solution.getNumberOfVariables(); ++i) {
      final var candidates = new ArrayList<Integer>();

      final var bitset = ((BinaryVariable) solution.getVariable(i)).getBitSet();
      for (int k = bitset.nextSetBit(0); k >= 0; k = bitset.nextSetBit(k + 1)) {
        candidates.add(k);
      }

      resourceCandidates.put(i, candidates);
    }

    cost = solution.getObjectiveValue(0);
  }

  /**
   * Constructs a new CandidateAssignments instance with the specified resource candidates and cost.
   * @param resourceCandidates a map of resource indices to their candidate service indices
   * @param cost the total cost associated with these assignments
   */
  @JsonCreator
  public CandidateAssignments(
    @JsonProperty("assignments") Map<Integer, List<Integer>> resourceCandidates,
    @JsonProperty("cost") double cost
  ) {
    this.resourceCandidates = resourceCandidates;
    this.cost = cost;
  }

  /**
   * Returns the resource candidates mapping.
   * @return a map where keys are resource indices and values are lists of candidate service indices
   */
  public double getCost() {
    return cost;
  }

  /**
   * Returns the mapping of resource indices to their candidate service indices.
   * @return a map of resource indices to lists of candidate service indices
   */
  @JsonIgnore
  public int getNumResources() {
    return resourceCandidates.size();
  }

  /**
   * Returns the candidates for a specific resource index.
   * @param i the resource index
   * @return a list of candidate service indices for the specified resource
   */
  public List<Integer> getCandidates(int i) {
    return resourceCandidates.getOrDefault(i, new ArrayList<>());
  }

  /**
   * Returns the mapping of resource indices to their candidate service indices.
   * @return a map of resource indices to lists of candidate service indices
   */
  @Override
  public String toString() {
    return String.format("CandidateAssignments{assignments=%s, cost=%s}", resourceCandidates, cost);
  }
}
