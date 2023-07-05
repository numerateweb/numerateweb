/*
 * Copyright (c) 2023 Numerate Web contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.numerateweb.math.cli;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;

public class CLI {
	Map<String, CLICommand> commands = new LinkedHashMap<>();

	{
		for (CLICommand c : Arrays.<CLICommand> asList(new ConvertOMObject(), //
				new PopcornPatternSearch(), //
				new ConvertOMCD(), //
				new ValidateOMCD(), //
				new RulesToRdf(), //
				new PopcornToSparql() //
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
