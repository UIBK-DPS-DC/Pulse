package at.ac.uibk.dps.pulse.utils.common;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.BitSet;
import java.util.Objects;

/**
 * Represents a 3D cube of bits, allowing for efficient storage and manipulation of boolean values
 * in a three-dimensional space.
 * The cube is defined by its dimensions (x, y, z) and contains a BitSet to store the bit values.
 */
public final class BitCube {

  private final int x;
  private final int y;
  private final int z;
  private final BitSet data;

  /**
   * Constructs a new bit cube instance with the specified dimensions and raw data.
   * @param x        the size in the x dimension
   * @param y        the size in the y dimension
   * @param z        the size in the z dimension
   * @param rawData  the raw data as an array of long values, representing the bit set
   */
  @JsonCreator
  public BitCube(
    @JsonProperty("x") final int x,
    @JsonProperty("y") final int y,
    @JsonProperty("z") final int z,
    @JsonProperty("data") final long[] rawData
  ) {
    this.x = x;
    this.y = y;
    this.z = z;
    this.data = BitSet.valueOf(rawData);
  }

  /**
   * Constructs a new bit cube instance with the specified dimensions.
   * @param x the size in the x dimension
   * @param y the size in the y dimension
   * @param z the size in the z dimension
   */
  public BitCube(final int x, final int y, final int z) {
    this.x = x;
    this.y = y;
    this.z = z;
    this.data = new BitSet(x * y * z);
  }

  /**
   * Returns a slice of the cube along the z-axis at the specified x and y coordinates.
   * @param x the x coordinate
   * @param y the y coordinate
   * @return a bit set representing the z-line at the specified x and y coordinates
   */
  public BitSet getZLine(final int x, final int y) {
    if (x < 0 || x >= this.x) throw new IndexOutOfBoundsException("x: " + x);
    if (y < 0 || y >= this.y) throw new IndexOutOfBoundsException("y: " + y);

    final var slice = new BitSet(z);
    for (int i = 0; i < z; ++i) {
      if (get(x, y, i)) {
        slice.set(i);
      }
    }
    return slice;
  }

/**
   * Returns a slice of the cube along the y-axis at the specified x and z coordinates.
   * @param x the x coordinate
   * @param z the z coordinate
   * @return a bit set representing the y-line at the specified x and z coordinates
   */
  public BitSet getYLine(final int x, final int z) {
    if (x < 0 || x >= this.x) throw new IndexOutOfBoundsException("x: " + x);
    if (z < 0 || z >= this.z) throw new IndexOutOfBoundsException("z: " + z);

    final var slice = new BitSet(y);
    for (int i = 0; i < y; ++i) {
      if (get(x, i, z)) {
        slice.set(i);
      }
    }
    return slice;
  }

  /**
   * Returns a slice of the cube along the x-axis at the specified y and z coordinates.
   * @param y the y coordinate
   * @param z the z coordinate
   * @return a bit set representing the x-line at the specified y and z coordinates
   */
  public BitSet getXLine(final int y, final int z) {
    if (y < 0 || y >= this.y) throw new IndexOutOfBoundsException("y: " + y);
    if (z < 0 || z >= this.z) throw new IndexOutOfBoundsException("z: " + z);

    final var slice = new BitSet(x);
    for (int i = 0; i < x; ++i) {
      if (get(i, y, z)) {
        slice.set(i);
      }
    }
    return slice;
  }

  /**
   * Retrieves the boolean value at the specified coordinates in the cube.
   * @param x the x coordinate
   * @param y the y coordinate
   * @param z the z coordinate
   * @return the boolean value at the specified coordinates
   */
  public boolean get(final int x, final int y, final int z) {
    return data.get(index(x, y, z));
  }

  /**
   * Sets the boolean value at the specified coordinates in the cube.
   * @param x     the x coordinate
   * @param y     the y coordinate
   * @param z     the z coordinate
   * @param value the boolean value to set
   */
  public void set(final int x, final int y, final int z, final boolean value) {
    data.set(index(x, y, z), value);
  }

  /**
   * Flips the boolean value at the specified coordinates in the cube.
   * @param x the x coordinate
   * @param y the y coordinate
   * @param z the z coordinate
   */
  public void flip(final int x, final int y, final int z) {
    data.flip(index(x, y, z));
  }

  /**
   * Checks if the cube is empty, meaning all bits are unset.
   * @return true if the cube is empty, false otherwise
   */
  public void clear() {
    data.clear();
  }

  private int index(final int x, final int y, final int z) {
    return x * (this.y * this.z) + y * this.z + z;
  }

  /**
   * Returns the size of the cube in the x dimension.
   * @return the size in the x dimension
   */
  @JsonProperty("x")
  public int getX() {
    return x;
  }

  /**
   * Returns the size of the cube in the y dimension.
   * @return the size in the y dimension
   */
  @JsonProperty("y")
  public int getY() {
    return y;
  }

  /**
   * Returns the size of the cube in the z dimension.
   * @return the size in the z dimension
   */
  @JsonProperty("z")
  public int getZ() {
    return z;
  }

  /**
   * Returns the raw data of the cube as an array of long values.
   * @return the raw data as an array of long values
   */
  @JsonProperty("data")
  public long[] getRawData() {
    return data.toLongArray();
  }

  /**
   * Checks if this bit cube is equal to another object.
   * @param obj the object to compare with
   * @return true if the object is a bit cube with the same dimensions and data, false otherwise
   */
  @Override
  public boolean equals(final Object obj) {
    if (!(obj instanceof BitCube other)) return false;
    return (
      this.x == other.x &&
      this.y == other.y &&
      this.z == other.z &&
      Objects.equals(this.data, other.data)
    );
  }

  /**
   * Returns a string representation of the bit cube.
   * @return a string representation of the bit cube
   */
  @Override
  public int hashCode() {
    return Objects.hash(x, y, z, data);
  }
}
