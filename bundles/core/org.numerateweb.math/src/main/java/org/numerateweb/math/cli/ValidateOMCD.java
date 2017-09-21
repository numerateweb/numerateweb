package org.numerateweb.math.cli;

import java.util.Arrays;

import net.enilink.komma.core.URIs;

import org.eclipse.core.runtime.IStatus;
import org.numerateweb.math.xml.OMReader;

public class ValidateOMCD implements CLICommand {
	@Override
	public String description() {
		return "Validates the syntax of one or more OpenMath content dictionaries.";
	}

	@Override
	public String name() {
		return "validate-cd";
	}

	public void run(String... args) {
		if (args.length == 0) {
			System.err.println("Usage: " + usage());
			return;
		}
		for (String file : args) {
			IStatus status = new OMReader(Arrays.asList("ocd")).readAll(
					URIs.createFileURI(file), new CLIProgressMonitor());
			if (!status.isOK()) {
				StatusUtil.printStatus(System.err, status);
			}
		}
	}

	@Override
	public String usage() {
		return name() + " file [file1 ...]";
	}
}