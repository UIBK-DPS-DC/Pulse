package at.ac.uibk.dps.pulse.local.selection;

import java.util.Optional;
import org.moeaframework.core.Solution;

/**
 * Represents a contract for selection strategies that operate on a set of potential solutions
 * to choose one according to specific criteria.
 */
public interface Selector {
  
  /**
   * Selects a solution from the underlying population based on a specific selection strategy.
   * @return the selected solution, or empty
   */
  Optional<Solution> select();
}
