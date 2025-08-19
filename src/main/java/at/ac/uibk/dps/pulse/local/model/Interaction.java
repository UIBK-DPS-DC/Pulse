package at.ac.uibk.dps.pulse.local.model;

/**
 * Represents an interaction between resources in the Pulse scheduling framework.
 * Each interaction has a weight and a data transfer amount associated with it.
 * @param weight        the weight of the interaction
 * @param dataTransfer  the amount of data transferred during the interaction
 */
public record Interaction(double weight, double dataTransfer) {}
