package at.ac.uibk.dps.pulse.local.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.math.DoubleMath;
import java.util.Map;

/**
 * Represents a service in the Pulse scheduling framework.
 * Each service has a name, an image name, interactions with other services,
 * data requirements, number of replicas, and resource requirements.
 */
public class Service {

  private final String serviceName;

  private final String imageName;

  private final Map<String, Interaction> interactions;

  private final double data;

  private final int replicas;

  private final Characteristics requirements;

  /**
   * Constructs a new service instance.
   * @param serviceName   the name of the service
   * @param imageName     the name of the image used for the service
   * @param interactions  a map of interactions with other services
   * @param data          the amount of data required by the service
   * @param replicas      the number of replicas for the service
   * @param requirements  the resource requirements for the service
   */
  @JsonCreator
  public Service(
    @JsonProperty("serviceName") String serviceName,
    @JsonProperty("imageName") String imageName,
    @JsonProperty("interactions") Map<String, Interaction> interactions,
    @JsonProperty("data") double data,
    @JsonProperty("replicas") int replicas,
    @JsonProperty("requirements") Characteristics requirements
  ) {
    this.serviceName = serviceName;
    this.imageName = imageName;
    this.interactions = interactions;
    this.data = data;
    this.replicas = replicas;
    this.requirements = requirements;
  }

  /**
   * Returns the name of the service.
   * @return the name of the service
   */
  public String getServiceName() {
    return serviceName;
  }

  /**
   * Returns the name of the image used for the service.
   * @return the name of the image
   */
  public String getImageName() {
    return imageName;
  }

  /**
   * Returns the interactions with other services.
   * @return a map of interactions where keys are service names and values are Interaction objects
   */
  public Map<String, Interaction> getInteractions() {
    return interactions;
  }

  /**
   * Returns the amount of data stored by the service.
   * @return the data requirement
   */
  public double getData() {
    return data;
  }

  /**
   * Returns the number of replicas for the service.
   * @return the number of replicas
   */
  public int getReplicas() {
    return replicas;
  }

  /**
   * Returns the resource requirements for the service.
   * @return the characteristics of the service's resource requirements
   */
  public Characteristics getRequirements() {
    return requirements;
  }

  /**
   * Checks if this service is equal to another object.
   * @param obj the object to compare with
   * @return true if the object is a service with the same properties, false otherwise
   */
  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof Service other)) {
      return false;
    }
    return (
      serviceName.equals(other.serviceName) &&
      imageName.equals(other.imageName) &&
      interactions.equals(other.interactions) &&
      DoubleMath.fuzzyEquals(data, other.data, 0.00001) &&
      replicas == other.replicas &&
      requirements.equals(other.requirements)
    );
  }

  /**
   * Returns a string representation of the service.
   * @return a string describing the service
   */
  @Override
  public String toString() {
    return String.format(
      "Service{serviceName=%s, imageName=%s, interactions=%s, data=%f, replicas=%d, requirements=%s}",
      getServiceName(),
      getImageName(),
      getInteractions(),
      getData(),
      getReplicas(),
      getRequirements()
    );
  }
}
