package org.numerateweb.math.cli;

import java.io.PrintStream;

import org.eclipse.core.runtime.NullProgressMonitor;

/**
 * Reports progress to {@link PrintStream}.
 * 
 */
public class CLIProgressMonitor extends NullProgressMonitor {
	protected String task, subTask;
	protected int totalWork;
	protected double worked;
	protected PrintStream out = System.out;

	public CLIProgressMonitor() {
	}

	public CLIProgressMonitor(PrintStream out) {
		if (out != null) {
			this.out = out;
		}
	}

	@Override
	public void beginTask(String name, int totalWork) {
		this.totalWork = totalWork;
		this.worked = 0;
		task = name;
		out.println(task);
	}

	@Override
	public void done() {
	}

	@Override
	public void internalWorked(double work) {
		worked += work;
		update();
	}

	@Override
	public void subTask(String name) {
		subTask = name;
	}

	protected void update() {
		int labelWidth = 50;
		String label = (subTask == null ? "" : subTask);
		if (label.length() == 0 && task != null) {
			label = task;
		}
		if (label.length() > labelWidth) {
			label = label.substring(0, labelWidth - 4) + "...";
		}
		out.print(String.format("%-" + labelWidth + "s", label));

		final int barWidth = 79 - labelWidth;
		int i = 0;
		double progressPercentage = Math.min(worked / totalWork, 1.0);
		if (Double.NaN == progressPercentage) {
			progressPercentage = 0;
		}
		for (; i < (int) (progressPercentage * barWidth); i++) {
			out.print(".");
		}
		if (i < barWidth) {
			out.print(String.format("%-" + (barWidth - i) + "s", ""));
		}
		out.println("%");
	}

	@Override
	public void worked(int work) {
		internalWorked(work);
	}
}