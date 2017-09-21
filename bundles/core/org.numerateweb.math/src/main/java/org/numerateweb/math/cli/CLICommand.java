package org.numerateweb.math.cli;

/**
 * Interface for a command that can be executed through the standard
 * command-line-interface.
 * 
 */
public interface CLICommand {
	String description();

	String name();

	void run(String... args);

	String usage();
}
