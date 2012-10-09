package uk.ac.ox.pancik.lunarlander;

import java.io.File;
import java.io.IOException;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;

public class Headless {

	static public void main(final String args[]) {
		int numberOfSteps = 150;

		boolean usingBoltzmann = false;
		double temperature = 0.05;
		double randomActionsRatio = 0.05;
		double futureDiscountRate = 0.9;
		double traceDecayRate = 0.9;
		double learningRate = 0.05;

		String filename = "result.json";

		final Options options = new Options();

		// TODO fix descriptions
		options.addOption(OptionBuilder.withArgName("episodes").hasArg().withDescription("number of steps").create("i"));
		options.addOption(OptionBuilder.withArgName("gamma").hasArg().withDescription("setFutureDiscountRate").create("g"));
		options.addOption(OptionBuilder.withArgName("lambda").hasArg().withDescription("setTraceDecayRate of steps").create("l"));
		options.addOption(OptionBuilder.withArgName("alpha").hasArg().withDescription("setLearningRate").create("a"));
		options.addOption(OptionBuilder.withArgName("epsilon").hasArg().withDescription("setRandomActionsRatio").create("e"));
		options.addOption(OptionBuilder.withArgName("filename").hasArg().withDescription("filename").create("f"));
		options.addOption(OptionBuilder.withArgName("boltzmann").hasArg().withDescription("setUsingBoltzmann").create("b"));
		options.addOption(OptionBuilder.withArgName("temperature").hasArg().withDescription("setTemperature").create("t"));
		

		// TODO width a height

		final CommandLineParser parser = new PosixParser();

		// TODO error messages
		CommandLine line;
		try {
			line = parser.parse(options, args);
			
			if (line.hasOption("i")) {
				System.out.println("Using " + line.getOptionValue("i") + " steps");
				numberOfSteps = Integer.parseInt(line.getOptionValue("i"));
			} else {
				System.out.println("Using default number of steps");
			}

			if (line.hasOption("t")) {
				temperature = Double.parseDouble(line.getOptionValue("t"));
			}

			if (line.hasOption("b")) {
				usingBoltzmann = Boolean.valueOf(line.getOptionValue("b"));
			}
			
			if (line.hasOption("f")) {
				filename = line.getOptionValue("f");
			}
			
			if (line.hasOption("e")) {
				randomActionsRatio = Double.parseDouble(line.getOptionValue("e"));
			}
			
			if (line.hasOption("l")) {
				traceDecayRate = Double.parseDouble(line.getOptionValue("l"));
			}
			
			if (line.hasOption("g")) {
				futureDiscountRate = Double.parseDouble(line.getOptionValue("g"));
			}

			if (line.hasOption("a")) {
				learningRate = Double.parseDouble(line.getOptionValue("a"));
			}
		} catch (final ParseException e) {
			final HelpFormatter formatter = new HelpFormatter();
			formatter.printHelp("SarsaLander", options);
			System.exit(1);
		}

		final Controller controller = new Controller(500, 500, new int[] { 5, 5 }, usingBoltzmann, temperature, randomActionsRatio, futureDiscountRate, traceDecayRate, learningRate, numberOfSteps-100);

		while(controller.getLearningEpisodes() < numberOfSteps){
			controller.step();
		}
		
		try {
			controller.getSarsaAgent().saveToFile(new File(filename));
		} catch (final IOException e) {
			System.out.println("There was an error when saving final resutlts to file");
		}
		
		System.out.println("Done.");
	}
}
