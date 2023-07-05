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
package org.numerateweb.math.popcorn;

import org.numerateweb.math.ns.INamespaces;

public class PopcornBuilder extends PopcornBuilderBase<PopcornExpr> {
	public PopcornBuilder(INamespaces ns) {
		super(ns);
	}

	@Override
	protected PopcornExpr build(PopcornExpr expr) {
		return expr;
	}
}
