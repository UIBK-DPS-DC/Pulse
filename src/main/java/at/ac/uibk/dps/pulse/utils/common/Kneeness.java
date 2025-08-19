package at.ac.uibk.dps.pulse.utils.common;

import org.moeaframework.core.Solution;

/**
 * Utility class for computing the kneeness of a solution with respect to two reference solutions.
 * The kneeness is defined as the perpendicular distance from the solution to the line connecting
 * the two reference solutions.
 */
public class Kneeness {

  /**
   * Computes the kneeness of a solution with respect to two reference solutions.
   * @param p0 the first reference solution
   * @param p1 the second reference solution
   * @param p  the solution for which to compute the kneeness
   * @return the kneeness value, which is the perpendicular distance from p to the line defined by p0 and p1
   */
  public static double compute(Solution p0, Solution p1, Solution p) {
    final var x1 = p0.getObjectiveValue(0);
    final var y1 = p0.getObjectiveValue(1);
    final var x2 = p1.getObjectiveValue(0);
    final var y2 = p1.getObjectiveValue(1);

    final var vx = x2 - x1;
    final var vy = y2 - y1;
    final var lineLength = Math.sqrt(vx * vx + vy * vy);
    if (lineLength < 1e-12) {
      return 0.0;
    }

    final var ux = vx / lineLength;
    final var uy = vy / lineLength;

    final var px = p.getObjectiveValue(0);
    final var py = p.getObjectiveValue(1);

    final var wx = px - x1;
    final var wy = py - y1;

    final var projLen = wx * ux + wy * uy;

    final var perpX = wx - projLen * ux;
    final var perpY = wy - projLen * uy;

    return Math.sqrt(perpX * perpX + perpY * perpY);
  }
}
