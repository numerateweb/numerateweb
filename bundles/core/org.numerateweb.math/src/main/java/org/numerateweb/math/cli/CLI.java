package org.numerateweb.math.cli;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;

import net.enilink.commons.logging.LoggingPlugin;

public class CLI {
	Map<String, CLICommand> commands = new LinkedHashMap<>();

	{
		for (CLICommand c : Arrays.<CLICommand> asList(new ConvertOMObject(), //
				new PopcornPatternSearch(), //
				new ConvertOMCD(), //
				new ValidateOMCD() //
		)) {
			commands.put(c.name(), c);
		}
		commands.put("help", new CLICommand() {
			@Override
			public void run(String... args) {
				if (args.length > 0) {
					CLICommand cmd = commands.get(args[0]);
					if (cmd == null) {
						System.err.println("Unknown command '" + args[0] + "'");
					} else {
						String desc = cmd.description();
						if (desc != null && desc.length() > 0) {
							System.out.println(desc);
						}
						System.out.println(cmd.usage());
					}
				} else {
					System.err.println("Usage: " + usage());
				}
			}

			@Override
			public String name() {
				return "help";
			}

			@Override
			public String usage() {
				return name() + " command";
			}

			@Override
			public String description() {
				return "Display usage information for a command.";
			}
		});
	}

	public CLI(String... args) {
		CLICommand cmd = null;
		if (args.length > 0) {
			cmd = commands.get(args[0]);
		}
		if (cmd == null) {
			System.out.println("Available commands:");
			for (CLICommand c : commands.values()) {
				System.out.print(String.format("  %1$-15s", c.name()));
				String desc = c.description();
				System.out.println(desc != null && desc.length() > 0 ? "  " + desc : "");
			}
		} else {
			cmd.run(Arrays.copyOfRange(args, 1, args.length));
		}
	}

	public static void main(String... args) {
		// LoggingPlugin.init();
		new CLI(args);
	}
}
