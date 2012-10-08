package uk.ac.ox.pancik.lunarlander.rewarders;

import uk.ac.ox.pancik.lunarlander.Simulation;
import uk.ac.ox.pancik.sarsa.NeuralNetworkApproximator;

public class DelayedRewarderLanding implements Rewarder {

	private final Simulation simulation;

	public DelayedRewarderLanding(final Simulation simulation) {
		this.simulation = simulation;
	}

	@Override
	public double calculateReward() {
		final double speed = this.simulation.getVehicle().getVelocity().length();

		if (this.simulation.getTime() == Simulation.MAX_TIME) {
			return -1;
		} else if (this.simulation.isVehicleLanded()) {
			System.out.print('.');
			return NeuralNetworkApproximator.activationFunction(300.0 / speed / this.simulation.getTime());
		} else if (this.simulation.vehicleCollides()) {
			return -1;
		} else {
			return 0;
		}
	}
}
