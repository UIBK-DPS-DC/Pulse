package at.ac.uibk.dps.pulse.local.selection;

import java.util.Comparator;
import java.util.Optional;
import java.util.stream.StreamSupport;
import org.moeaframework.core.Solution;
import org.moeaframework.core.population.NondominatedPopulation;
import org.moeaframework.core.population.Population;

/**
 * Implements a selection strategy to select a solution from a nondominated population
 * based on a preference value that indicates the desired position in the sorted order of solutions.
 */
public class PreferenceSelector implements Selector {

  private final Population population;

  private final double preference;

  /**
   * Constructs a new preference selector instance.
   * @param population the nondominated population from which to select a solution
   * @param preference the preference value (between 0 and 1) indicating the desired position in the sorted order
   */
  public PreferenceSelector(final NondominatedPopulation population, final double preference) {
    this.population = population.filter(Solution::isFeasible);
    this.preference = preference;
  }

  @Override
  public Optional<Solution> select() {
    if (population.isEmpty()) {
      return Optional.empty();
    }

    if (population.size() == 1) {
      return Optional.of(population.get(0));
    }

    final var sorted = StreamSupport.stream(population.spliterator(), false)
      .sorted(Comparator.comparingDouble(s -> s.getObjectiveValue(0)))
      .toList();

    return Optional.of(sorted.get((int) Math.round(preference * (sorted.size() - 1))));
  }
}
