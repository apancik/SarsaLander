package uk.ac.ox.pancik.lunarlander;

import processing.core.PApplet;
import uk.ac.ox.pancik.lunarlander.entities.Obstacle;
import uk.ac.ox.pancik.lunarlander.sensors.DistanceSensor;
import uk.ac.ox.pancik.sarsa.NeuralNetworkApproximator;
import uk.ac.ox.pancik.sarsa.sensors.Sensor;
import uk.ac.ox.pancik.utils.Vector2D;

import com.google.common.primitives.Floats;

public class View extends PApplet {
	private static final long serialVersionUID = 7006333570347201593L;

	static public void main(final String args[]) {
		PApplet.main("uk.ac.ox.pancik.lunarlander.View");
	}

	private Controller controller;

	private boolean fast;

	@Override
	public void draw() {
		// PERFORM LOGIC
		for (int iteration = 0; iteration < (this.fast ? 1000 : 2); iteration++) {
			this.controller.step();
		}

		// DISPLAY
		this.background(51);

		this.drawMoon();

		this.drawLander();

		this.drawHistory();

		this.drawActions();
	}

	private void drawActions() {
		// ===============
		// DISPLAY ACTIONS
		// ===============
		final NeuralNetworkApproximator[] neuralNetworksArray = this.controller.getSarsaAgent().getNeuralNetworks();
		this.strokeWeight(10);

		for (int i = 0; i < neuralNetworksArray.length; i++) {
			final float utility = (float) neuralNetworksArray[i].getPredictedUtility();
			this.setConditionalColor(utility);

			this.line(this.width / 2f, this.height * 0.9f - i * 10f, this.width / 2f + utility * this.width / 4f, this.height * 0.9f - i * 10f);
		}

		// ==============
		// DISPLAY REWARD
		// ==============
		this.strokeWeight(1500f * (float) Math.abs(this.controller.getRewarder().calculateReward()));
		this.setConditionalColor(this.controller.getRewarder().calculateReward());
		this.point(50, this.height * 0.85f);

		this.strokeWeight(3);
	}

	private void drawHistory() {
		final float maximum = Floats.max(this.controller.getHistoryArray()) + 0.0001f;

		final float bottomMargin = 0.8f; // 80% margin from bottom

		final float increment = this.width / (Controller.HISTORY_SIZE - 1f);
		float x = 0;

		this.stroke(255);
		this.strokeWeight(2);
		for (int i = 0; i < Controller.HISTORY_SIZE - 1; i++) {
			if (this.controller.getHistoryArray()[i + 1] > 0 && this.controller.getHistoryPointer() != i + 1) {
				this.line(x, this.height - this.height * (bottomMargin + this.controller.getHistoryArray()[i] / maximum * (1f - bottomMargin)), x + increment, this.height - this.height * (bottomMargin + this.controller.getHistoryArray()[i + 1] / maximum * (1f - bottomMargin)));
			}

			if (i + 1 == this.controller.getHistoryPointer() && this.controller.getHistoryArray()[i] > 0) {
				this.strokeWeight(10);
				this.point(increment * i, this.height - this.height * (bottomMargin + this.controller.getHistoryArray()[i] / maximum * (1f - bottomMargin)));
				this.strokeWeight(2);
			}

			x += increment;
		}

		this.strokeWeight(3);
	}

	private void drawLander() {
		this.ellipse((float) this.controller.getVehicle().getPosition().getX(), (float) this.controller.getVehicle().getPosition().getY(), this.controller.getVehicle().getRadius(), this.controller.getVehicle().getRadius());

		for (final Sensor sensor : this.controller.getSensors()) {
			if (sensor instanceof DistanceSensor) {
				final DistanceSensor distanceSensor = (DistanceSensor) sensor;
				final double distance = distanceSensor.detect();
				if (distance < 1) {
					this.stroke(193, 231, 102);
					this.line(this.controller.getVehicle().getPosition(), distanceSensor.getCurrentSensorDirection(), distance * distanceSensor.getMaxDistance());
				} else {
					this.stroke(93, 168, 211);
					this.line(this.controller.getVehicle().getPosition(), distanceSensor.getCurrentSensorDirection(), this.controller.getVehicle().getRadius());
				}
			}
		}

		Math.sin(this.controller.getVehicle().getAngle());
		Math.cos(this.controller.getVehicle().getAngle());
		final int action = this.controller.getSarsaAgent().getSelectedActionIndex();
		if (action < 2) {
			this.stroke(239, 125, 98);
			this.strokeWeight(10); // Beastly
			this.line(this.controller.getVehicle().getPosition(), this.controller.getVehicle().getDirection().times(-1), action == 0 ? 30 : 20);
			this.strokeWeight(3);
		}
	}

	private void drawMoon() {
		for (final Obstacle obstacle : this.controller.getSimulation().getObstacles()) {
			this.stroke(255);
			this.fill(255);
			this.ellipse((float) obstacle.getPosition().getX(), (float) obstacle.getPosition().getY(), obstacle.getRadius(), obstacle.getRadius());
		}
	}

	private void line(final Vector2D position, final Vector2D direction, final double length) {
		final Vector2D end = position.plus(direction.normalize().times(length));
		this.line((float) position.getX(), (float) position.getY(), (float) end.getX(), (float) end.getY());
	}

	@Override
	public void mouseClicked() {
		this.fast = !this.fast;
	}

	private void setConditionalColor(final double value) {
		if (value > 0) {
			this.stroke(193, 231, 102);
		} else {
			this.stroke(239, 125, 98);
		}
	}

	@Override
	public void setup() {
		this.strokeWeight(3);
		this.size(800, 600);
		this.ellipseMode(RADIUS);

		boolean usingBoltzmann = false;
		double temperature = 0.05;
		double randomActionsRatio = 0.05;
		double futureDiscountRate = 0.99;
		double traceDecayRate = 0.99;
		double learningRate = 0.01;
		
		this.controller = new Controller(this.width, this.height, new int[] { 5, 5 }, usingBoltzmann, temperature, randomActionsRatio, futureDiscountRate, traceDecayRate, learningRate, 10000);
	}
}
