package at.ac.uibk.dps.pulse.local.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Represents a resource in the Pulse scheduling framework.
 * Each resource has a name and characteristics that define its capabilities.
 * The cost of the resource is calculated based on its characteristics.
 */
public class Resource {

  private final String resourceName;

  private final Characteristics characteristics;

  /**
   * Constructs a new resource instance.
   * @param resourceName the name of the resource
   * @param characteristics the characteristics of the resource
   */
  @JsonCreator
  public Resource(
    @JsonProperty("resourceName") String resourceName,
    @JsonProperty("characteristics") Characteristics characteristics
  ) {
    this.resourceName = resourceName;
    this.characteristics = characteristics;
  }

  /**
   * Returns the name of the resource.
   * @return the name of the resource
   */
  public String getResourceName() {
    return resourceName;
  }

  /**
   * Returns the characteristics of the resource.
   * @return the characteristics of the resource
   */
  public Characteristics getCharacteristics() {
    return characteristics;
  }

  /**
   * Returns the CPU value of the resource's characteristics.
   * @return the CPU value
   */
  @JsonIgnore
  public Cost getCost() {
    return new Cost(
      0.0366 * characteristics.getCpu() +
      0.0043 * characteristics.getMemory() +
      0.0001 * characteristics.getDisk() +
      1.6760 * characteristics.getGpu(),
      0.0,
      0.05,
      0.09
    );
  }

  /**
   * Returns the CPU value of the resource's characteristics.
   * @return the CPU value
   */
  @Override
  public String toString() {
    return String.format(
      "Resource{name=%s, characteristics=%s, cost=%f}",
      resourceName,
      characteristics,
      getCost().sum()
    );
  }

  /**
   * Represents the cost associated with a resource.
   * The cost is calculated based on fixed costs, data transfer costs, and input/output costs.
   */
  public record Cost(double fixed, double data, double in, double out) {

    /**
     * Returns the sum of all cost components.
     * @return the total cost
     */
    public double sum() {
      return fixed + data + in + out;
    }
  }
}
