package org.numerateweb.math.cli;

import java.io.PrintStream;

import org.eclipse.core.runtime.IStatus;

public class StatusUtil {
	public static void printStatus(PrintStream out, IStatus status) {
		print(out, "", status);
	}

	private static void print(PrintStream out, String indent, IStatus status) {
		if (status.isMultiStatus()) {
			out.println(indent + status.getMessage().replace('\n', ' '));
			String childIndent = indent + "  ";
			for (IStatus c : status.getChildren()) {
				print(out, childIndent, c);
			}
		} else {
			out.println(indent + status.getMessage().replace('\n', ' '));
		}
	}
}
