package at.ac.uibk.dps.pulse.local.selection;

import at.ac.uibk.dps.pulse.utils.common.Kneeness;
import java.util.Comparator;
import java.util.Optional;
import java.util.stream.IntStream;
import org.moeaframework.core.Solution;
import org.moeaframework.core.population.NondominatedPopulation;
import org.moeaframework.core.population.Population;

/**
 * Implements a selection strategy to select a solution from a nondominated population
 * using a kneeness function.
 */
public class KneenessSelector implements Selector {

  private final Population population;

  /**
   * Constructs a new kneenesss selector instance.
   * @param population the nondominated population from which to select a solution
   */
  public KneenessSelector(final NondominatedPopulation population) {
    this.population = population.filter(Solution::isFeasible);
  }

  @Override
  public Optional<Solution> select() {
    if (population.isEmpty()) {
      return Optional.empty();
    }

    if (population.size() < 3) {
      return Optional.of(population.get(0));
    }

    final var first = population.get(0);
    final var last = population.get(population.size() - 1);

    return IntStream.range(1, population.size() - 1)
      .mapToObj(population::get)
      .max(Comparator.comparingDouble(p -> Kneeness.compute(first, last, p)));
  }
}
