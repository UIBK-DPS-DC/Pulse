package at.ac.uibk.dps.pulse.global.model;

/**
 * Represents a candidate for a service in the Pulse scheduling framework.
 * Each candidate has an assigned status and a cost associated with it.
 * @param assigned indicates whether the candidate is assigned to a cluster
 * @param cost     the cost associated with this candidate
 */
public record Candidate(boolean assigned, double cost) {}
