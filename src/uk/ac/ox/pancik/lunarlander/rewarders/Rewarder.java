package uk.ac.ox.pancik.lunarlander.rewarders;

// implementation of eval(X) - utility of the actual state
public interface Rewarder {
	public double calculateReward();
}