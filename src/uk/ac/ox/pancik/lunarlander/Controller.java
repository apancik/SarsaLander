package uk.ac.ox.pancik.lunarlander;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import uk.ac.ox.pancik.lunarlander.actions.NoOperation;
import uk.ac.ox.pancik.lunarlander.actions.Thrust;
import uk.ac.ox.pancik.lunarlander.actions.Turn;
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

public class Controller {
	private int learningEpisodes;

	private boolean BACKUPS_ENABLED = true;

	int experimentLength = 1000;
	
	private int finalCounter = 0;

	private SarsaAgent backup;
	private int backupRate;

	public float[] getHistoryArray() {
		return historyArray;
	}
	
	public Rewarder getRewarder() {
		return rewarder;
	}
	
	public SarsaAgent getSarsaAgent() {
		return sarsaAgent;
	}
	
	private Simulation simulation;

	private SarsaAgent sarsaAgent;

	private Rewarder rewarder;

	private Action[] actionsArray;

	private Vehicle vehicle;
	private static final double RADAR_ANGLE = 0.4;
	private static final int NUMBER_OF_SENSORS = 3;

	private List<Sensor> sensors;

	public static int HISTORY_SIZE = 100;
	private final float[] historyArray = new float[HISTORY_SIZE];
	private int historyPointer = 0;

	public List<Sensor> getSensors() {
		return sensors;
	}
	
	private static int RESULTS_SIZE = 100;
	private final LinkedList<Integer> results = new LinkedList<Integer>();
	private final LinkedList<Integer> backupResults = new LinkedList<Integer>();

	float exponentialAverage = 0;
	
	public Simulation getSimulation() {
		return simulation;
	}
	
	private void backup() {
		System.out.println(">" + this.sum(this.results));
		this.backupRate = this.sum(this.results);
		this.backup = new SarsaAgent(this.sarsaAgent);
		Collections.copy(this.backupResults, this.results);
	}

	
	private int sum(final List<Integer> list) {
		int result = 0;
		for (final Integer i : list) {
			result += i;
		}
		return result;
	}
	
	public int getLearningEpisodes() {
		return learningEpisodes;
	}
	
	private void checkOnExperiment() {
		this.learningEpisodes++;

		if (this.learningEpisodes == experimentLength) {
			this.BACKUPS_ENABLED = false;
			this.recover();
			NeuralNetworkApproximator.setLearningRate(0);
			this.sarsaAgent.setUsingBoltzmann(false);
		}

		if (this.learningEpisodes > experimentLength) {
			this.finalCounter += this.simulation.getResult();
		}

		if (this.learningEpisodes == experimentLength + 100) {
			System.out.println("Final: " + this.finalCounter);
		}
	}
	
	public Controller(int width, int height, int[] networkStructure, boolean usingBoltzmann, double temperature, double randomActionsRatio, double futureDiscountRate, double traceDecayRate, double learningRate, int experimentLength) {
this.experimentLength = experimentLength;
		
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

		this.simulation = new Simulation(this.vehicle, this.sensors, width, height);

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
		
		this.sarsaAgent = new SarsaAgent(this.sensors.size(),  networkStructure, this.actionsArray.length);

		this.sarsaAgent.setUsingBoltzmann(false);
		this.sarsaAgent.setTemperature(0.05);
		this.sarsaAgent.setRandomActionsRatio(0.05);
		this.sarsaAgent.setFutureDiscountRate(0.99);

		NeuralNetworkApproximator.setTraceDecayRate(0.99);
		NeuralNetworkApproximator.setLearningRate(0.005);

		this.prepareTrial();
	}
	
	private void recover() {
		if (this.backup != null) {
			System.out.print("<");
			this.sarsaAgent = new SarsaAgent(this.backup);
			Collections.copy(this.results, this.backupResults);
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

	public int getHistoryPointer() {
		return historyPointer;
	}
	
	public Vehicle getVehicle() {
		return vehicle;
	}
	
	public void step() {
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
	
	
	
}
