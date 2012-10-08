package uk.ac.ox.pancik.lunarlander;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import processing.core.PApplet;
import uk.ac.ox.pancik.lunarlander.actions.NoOperation;
import uk.ac.ox.pancik.lunarlander.actions.Thrust;
import uk.ac.ox.pancik.lunarlander.actions.Turn;
import uk.ac.ox.pancik.lunarlander.entities.Obstacle;
import uk.ac.ox.pancik.lunarlander.entities.Vehicle;
import uk.ac.ox.pancik.lunarlander.rewarders.DelayedRewarderLanding;
import uk.ac.ox.pancik.lunarlander.rewarders.Rewarder;
import uk.ac.ox.pancik.lunarlander.sensors.DistanceSensor;
import uk.ac.ox.pancik.lunarlander.sensors.HorizontalVelocitySensor;
import uk.ac.ox.pancik.lunarlander.sensors.RotationSensor;
import uk.ac.ox.pancik.lunarlander.sensors.TimeSensor;
import uk.ac.ox.pancik.lunarlander.sensors.VerticalVelocitySensor;
import uk.ac.ox.pancik.sarsa.NeuralNetworkApproximator;
import uk.ac.ox.pancik.sarsa.SarsaAgent;
import uk.ac.ox.pancik.sarsa.actions.Action;
import uk.ac.ox.pancik.sarsa.sensors.Sensor;
import uk.ac.ox.pancik.utils.Vector2D;

import com.google.common.primitives.Floats;

public class Renderer extends PApplet {
	private static final long serialVersionUID = 7006333570347201593L;

	static int EXPERIMENT_END = 10000;

	static public void main(final String args[]) {
		PApplet.main(new String[] { "uk.ac.ox.pancik.lunarlander.Renderer" });
	}

	private int learningEpisodes;

	private boolean BACKUPS_ENABLED = true;

	private int finalCounter = 0;

	private SarsaAgent backup;
	private int backupRate;

	private Simulation simulation;

	private SarsaAgent sarsaAgent;

	private Rewarder rewarder;

	private Action[] actionsArray;

	private Vehicle vehicle;
	private static final double RADAR_ANGLE = 0.4;
	private static final int NUMBER_OF_SENSORS = 3;

	private boolean fast;

	private List<Sensor> sensors;

	private static int HISTORY_SIZE = 100;
	private final float[] historyArray = new float[HISTORY_SIZE];
	private int historyPointer = 0;

	private static int RESULTS_SIZE = 100;
	private final LinkedList<Integer> results = new LinkedList<Integer>();
	private final LinkedList<Integer> backupResults = new LinkedList<Integer>();

	float exponentialAverage = 0;

	private void backup() {
		System.out.println(">" + this.sum(this.results));
		this.backupRate = this.sum(this.results);
		this.backup = new SarsaAgent(this.sarsaAgent);
		Collections.copy(this.backupResults, this.results);
	}

	private void checkOnExperiment() {
		this.learningEpisodes++;

		if (this.learningEpisodes == Renderer.EXPERIMENT_END) {
			this.BACKUPS_ENABLED = false;
			this.recover();
			NeuralNetworkApproximator.setLearningRate(0);
			this.sarsaAgent.setUsingBoltzmann(false);
			System.out.println("\n Final experiment started ");
		}

		if (this.learningEpisodes > Renderer.EXPERIMENT_END) {
			this.finalCounter += this.simulation.getResult();
		}

		if (this.learningEpisodes == Renderer.EXPERIMENT_END + 100) {
			System.out.println("\n Final counter " + this.finalCounter);
		}
	}

	@Override
	public void draw() {
		// PERFORM LOGIC
		for (int iteration = 0; iteration < (this.fast ? 1000 : 1); iteration++) {
			this.performLogic();
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
		final NeuralNetworkApproximator[] neuralNetworksArray = this.sarsaAgent.getNeuralNetworks();
		this.strokeWeight(10);

		for (int i = 0; i < neuralNetworksArray.length; i++) {
			final float utility = (float) neuralNetworksArray[i].getPredictedUtility();
			this.setConditionalColor(utility);

			this.line(this.width / 2f, this.height * 0.9f - i * 10f, this.width / 2f + utility * this.width / 4f, this.height * 0.9f - i * 10f);
		}

		// ==============
		// DISPLAY REWARD
		// ==============
		this.strokeWeight(1500f * (float) Math.abs(this.rewarder.calculateReward()));
		this.setConditionalColor(this.rewarder.calculateReward());
		this.point(50, this.height * 0.85f);

		this.strokeWeight(3);
	}

	private void drawHistory() {
		final float maximum = Floats.max(this.historyArray) + 0.0001f;

		final float bottomMargin = 0.8f; // 80% margin from bottom

		final float increment = this.width / (HISTORY_SIZE - 1f);
		float x = 0;

		this.stroke(255);
		this.strokeWeight(2);
		for (int i = 0; i < HISTORY_SIZE - 1; i++) {
			if (this.historyArray[i + 1] > 0 && this.historyPointer != i + 1) {
				this.line(x, this.height - this.height * (bottomMargin + this.historyArray[i] / maximum * (1f - bottomMargin)), x + increment, this.height - this.height * (bottomMargin + this.historyArray[i + 1] / maximum * (1f - bottomMargin)));
			}

			if (i + 1 == this.historyPointer && this.historyArray[i] > 0) {
				this.strokeWeight(10);
				this.point(increment * i, this.height - this.height * (bottomMargin + this.historyArray[i] / maximum * (1f - bottomMargin)));
				this.strokeWeight(2);
			}

			x += increment;
		}

		this.strokeWeight(3);
	}

	private void drawLander() {
		this.ellipse((float) this.vehicle.getPosition().getX(), (float) this.vehicle.getPosition().getY(), this.vehicle.getRadius(), this.vehicle.getRadius());

		for (final Sensor sensor : this.sensors) {
			if (sensor instanceof DistanceSensor) {
				final DistanceSensor distanceSensor = (DistanceSensor) sensor;
				final double distance = distanceSensor.detect();
				if (distance < 1) {
					this.stroke(193, 231, 102);
					this.line(this.vehicle.getPosition(), distanceSensor.getCurrentSensorDirection(), distance * distanceSensor.getMaxDistance());
				} else {
					this.stroke(93, 168, 211);
					this.line(this.vehicle.getPosition(), distanceSensor.getCurrentSensorDirection(), this.vehicle.getRadius());
				}
			}
		}

		Math.sin(this.vehicle.getAngle());
		Math.cos(this.vehicle.getAngle());
		final int action = this.sarsaAgent.getSelectedActionIndex();
		if (action < 2) {
			this.stroke(239, 125, 98);
			this.strokeWeight(10); // Beastly
			this.line(this.vehicle.getPosition(), this.vehicle.getDirection().times(-1), action == 0 ? 30 : 20);
			this.strokeWeight(3);
		}
	}

	private void drawMoon() {
		for (final Obstacle obstacle : this.simulation.getObstacles()) {
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

	public void performLogic() {
		// ========
		// SIMULATE
		// ========

		this.simulation.step();

		// =====
		// LEARN
		// =====
		this.sarsaAgent.update(sensors, this.rewarder.calculateReward());

		// ======
		// REWARD
		// ======
		if (this.simulation.isEndState()) {
			// Save the result
			this.results.addLast(this.simulation.getResult());
			this.results.removeFirst();

			this.exponentialAverage = (float) (this.exponentialAverage * (1 - 0.0001) + this.simulation.getResult() * 0.0001);
			this.historyArray[this.historyPointer] = this.exponentialAverage;
			this.historyPointer = (this.historyPointer + 1) % HISTORY_SIZE;

			if (this.BACKUPS_ENABLED) {
				// Backup...
				if (this.backupRate * 1.1 < this.sum(this.results)) {
					this.backup();
				}
				// Recover
				if (this.sum(this.results) < this.backupRate - Math.abs(this.backupRate) * 0.5) {
					this.recover();
				}
			}

			this.checkOnExperiment();

			this.prepareTrial();
		} else {
			// Execute the selected action from the agent
			this.actionsArray[this.sarsaAgent.getSelectedActionIndex()].execute();
		}
	}

	private void prepareTrial() {
		// Reset the simulation state
		this.simulation.resetState();

		// Start a new learning trial
		this.sarsaAgent.startNewTrial();

		// Set the temperature for boltzmann exploration
		// this.sarsaAgent.setTemperature(this.INTERVAL_END + (this.INTERVAL_START - this.INTERVAL_END) * (1 - Math.min(1,
		// ++this.age / this.STEPS)));
	}

	private void recover() {
		if (this.backup != null) {
			System.out.print("<");
			this.sarsaAgent = new SarsaAgent(this.backup);
			Collections.copy(this.results, this.backupResults);
		}
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

		for (int i = 0; i < RESULTS_SIZE; i++) {
			this.results.add(0);
			this.backupResults.add(0);
		}

		// Create an vehicle to be controlled
		this.vehicle = new Vehicle();

		// Create an array of actions changing the state of the vehicle
		this.actionsArray = new Action[] { new Thrust(this.vehicle, Vehicle.MAX_THRUST), new Thrust(this.vehicle, Vehicle.MAX_THRUST / 3), new Turn(this.vehicle, Vehicle.TURNING_ANGLE), new Turn(this.vehicle, -Vehicle.TURNING_ANGLE), new NoOperation() };

		// Prepare Sensors
		this.sensors = new ArrayList<Sensor>();

		this.simulation = new Simulation(this.vehicle, this.sensors, this.width, this.height);

		this.sensors.add(new HorizontalVelocitySensor(this.vehicle));
		this.sensors.add(new VerticalVelocitySensor(this.vehicle));

		this.sensors.add(new RotationSensor(this.vehicle));

		this.sensors.add(new TimeSensor(this.simulation));

		for (int i = 0; i < NUMBER_OF_SENSORS; i++) {
			this.sensors.add(new DistanceSensor(this.simulation, this.vehicle, Math.PI + (i - 1) * RADAR_ANGLE));
		}

		// Prepare rewarder which maps the current state to an number
		this.rewarder = new DelayedRewarderLanding(this.simulation);

		// Initialize agent with actions
		// this.sarsaAgent = new SarsaAgent(this.sensors, new int[] { 10, 10 }, this.actionsArray.length); 21/100

		//this.sarsaAgent = new SarsaAgent(this.sensors, new int[] { 5, 5 }, this.actionsArray.length); //81/100
		
		this.sarsaAgent = new SarsaAgent(this.sensors.size(), new int[] { 5, 5 }, this.actionsArray.length);

		this.sarsaAgent.setUsingBoltzmann(false);
		this.sarsaAgent.setTemperature(0.05);
		this.sarsaAgent.setRandomActionsRatio(0.05);
		this.sarsaAgent.setFutureDiscountRate(0.99);

		NeuralNetworkApproximator.setTraceDecayRate(0.99);
		NeuralNetworkApproximator.setLearningRate(0.01);

		this.prepareTrial();
	}

	private int sum(final List<Integer> list) {
		int result = 0;
		for (final Integer i : list) {
			result += i;
		}
		return result;
	}
}
