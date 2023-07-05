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
