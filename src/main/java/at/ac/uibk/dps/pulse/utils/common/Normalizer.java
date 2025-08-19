package at.ac.uibk.dps.pulse.utils.common;

import java.util.ArrayList;
import java.util.List;

/**
 * A utility class for normalizing a set of double values.
 * It computes the minimum and maximum values from the input and provides methods to normalize
 * individual values as well as to compute normalized averages and sums.
 */
public final class Normalizer {

  private final List<Double> values = new ArrayList<>();

  private double min = Double.POSITIVE_INFINITY;

  private double max = Double.NEGATIVE_INFINITY;

  /**
   * Accepts a new value and updates the minimum and maximum values accordingly.
   * @param value the value to be added
   */
  public void accept(final double value) {
    values.add(value);
    min = Math.min(min, value);
    max = Math.max(max, value);
  }

  /**
   * Accepts multiple values and updates the minimum and maximum values accordingly.
   * @param values the values to be added
   */
  public void acceptAll(final double... values) {
    for (final var value : values) {
      accept(value);
    }
  }

  /**
   * Normalizes a given value based on the previously computed minimum and maximum values.
   * If the range is zero (i.e., all values are the same), it returns 0.0.
   * @param value the value to be normalized
   * @return the normalized value between 0.0 and 1.0
   */
  public double normalize(final double value) {
    if (values.isEmpty()) {
      return 0.0;
    }
    final var range = max - min;
    if (range == 0.0) {
      return 0.0;
    }
    return (value - min) / range;
  }

  /**
   * Calculates the normalized average of all accepted values.
   * @return the normalized average value
   */
  public double getNormalizedAverage() {
    return values.stream().mapToDouble(this::normalize).average().orElse(0.0);
  }

  /**
   * Calculates the normalized sum of all accepted values.
   * @return the normalized sum value
   */
  public double getNormalizedSum() {
    return values.stream().mapToDouble(this::normalize).sum();
  }
}
