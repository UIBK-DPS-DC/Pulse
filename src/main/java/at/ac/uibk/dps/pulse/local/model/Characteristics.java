package at.ac.uibk.dps.pulse.local.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.math.DoubleMath;
import java.util.Arrays;

/**
 * Represents the characteristics of a resource in terms of CPU, memory, disk, and GPU.
 * This class encapsulates the resource's capabilities and provides methods for comparison and arithmetic operations.
 */
public record Characteristics(double[] values) {

  public static final int CPU_INDEX = 0;
  public static final int MEMORY_INDEX = 1;
  public static final int DISK_INDEX = 2;
  public static final int GPU_INDEX = 3;

  /**
   * Constructs a new Characteristics instance with the specified values.
   * The values array must contain exactly four elements representing CPU, memory, disk, and GPU.
   * @param values an array of doubles representing the characteristics of the resource
   */
  @JsonCreator
  public Characteristics(@JsonProperty("values") double[] values) {
    assert values.length == 4;
    this.values = values;
  }

  /**
   * Constructs a new Characteristics instance with the specified CPU, memory, disk, and GPU values.
   * @param cpu the CPU value
   * @param memory the memory value
   * @param disk the disk value
   * @param gpu the GPU value
   */
  public Characteristics(double cpu, double memory, double disk, double gpu) {
    this(new double[] { cpu, memory, disk, gpu });
  }

  /**
   * Returns the CPU value of the resource characteristics.
   * @return the CPU value
   */
  @JsonIgnore
  public double getCpu() {
    return values[CPU_INDEX];
  }

  /**
   * Returns the memory value of the resource characteristics.
   * @return the memory value
   */
  @JsonIgnore
  public double getMemory() {
    return values[MEMORY_INDEX];
  }

  /**
   * Returns the disk value of the resource characteristics.
   * @return the disk value
   */
  @JsonIgnore
  public double getDisk() {
    return values[DISK_INDEX];
  }

  /**
   * Returns the GPU value of the resource characteristics.
   * @return the GPU value
   */
  @JsonIgnore
  public double getGpu() {
    return values[GPU_INDEX];
  }

  /**
   * Checks if this Characteristics instance is less than or equal to another Characteristics instance.
   * @param other the other characteristics instance to compare with
   * @return true if this instance is less than or equal to the other instance, false otherwise
   */
  public boolean leq(Characteristics other) {
    return (
      values[0] <= other.values[0] &&
      values[1] <= other.values[1] &&
      values[2] <= other.values[2] &&
      values[3] <= other.values[3]
    );
  }

  /**
   * Divides this characteristics instance by another Characteristics instance.
   * @param other the other characteristics instance to divide by
   * @return a new characteristics instance representing the result of the division
   */
  public Characteristics div(Characteristics other) {
    return new Characteristics(
      values[0] / (other.values[0] + 1.0e-10),
      values[1] / (other.values[1] + 1.0e-10),
      values[2] / (other.values[2] + 1.0e-10),
      values[3] / (other.values[3] + 1.0e-10)
    );
  }

  /**
   * Adds another characteristics instance to this instance.
   * @param other the other characteristics instance to add
   * @return a new characteristics instance representing the sum of this and the other instance
   */
  public Characteristics add(Characteristics other) {
    return new Characteristics(
      values[0] + other.values[0],
      values[1] + other.values[1],
      values[2] + other.values[2],
      values[3] + other.values[3]
    );
  }

  /**
   * Checks if this characteristics instance is equal to another Characteristics instance within a specified epsilon.
   * @param other the other characteristics instance to compare with
   * @param epsilon the tolerance for equality comparison
   * @return true if the instances are equal within the specified epsilon, false otherwise
   */
  public boolean equals(Characteristics other, double epsilon) {
    return (
      Math.abs(values[0] - other.values[0]) <= epsilon &&
      Math.abs(values[1] - other.values[1]) <= epsilon &&
      Math.abs(values[2] - other.values[2]) <= epsilon &&
      Math.abs(values[3] - other.values[3]) <= epsilon
    );
  }

  /**
   * Returns the minimum value among the characteristics (CPU, memory, disk, GPU).
   * @return the minimum value
   */
  public double max() {
    return Math.max(Math.max(values[0], values[1]), Math.max(values[2], values[3]));
  }

  /**
   * Returns the minimum value among the characteristics (CPU, memory, disk, GPU).
   * @return the minimum value
   */
  public double sum() {
    return Arrays.stream(values).sum();
  }

  /**
   * Checks if this characteristics instance is equal to another object.
   * @param obj the object to compare with
   * @return true if the object is a characteristics instance with equal values, false otherwise
   */
  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof Characteristics(double[] values1))) {
      return false;
    }
    return (
      DoubleMath.fuzzyEquals(values[CPU_INDEX], values1[CPU_INDEX], 0.00001) &&
      DoubleMath.fuzzyEquals(values[MEMORY_INDEX], values1[MEMORY_INDEX], 0.00001) &&
      DoubleMath.fuzzyEquals(values[DISK_INDEX], values1[DISK_INDEX], 0.00001) &&
      DoubleMath.fuzzyEquals(values[GPU_INDEX], values1[GPU_INDEX], 0.00001)
    );
  }

  /**
   * Returns a hash code for this characteristics instance.
   * @return the hash code
   */
  @Override
  public String toString() {
    return String.format(
      "Characteristics{cpu=%f, memory=%f, disk=%f, gpu=%f}",
      getCpu(),
      getMemory(),
      getDisk(),
      getGpu()
    );
  }
}
