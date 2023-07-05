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
package org.numerateweb.math.reasoner;

public class ResultSpec<T> {
	public static final ResultSpec<?> EMPTY = create(Cardinality.NONE, null);

	public Cardinality cardinality;
	public T result;

	protected ResultSpec(Cardinality cardinality, T result) {
		this.cardinality = cardinality;
		this.result = result;
	}

	public static <T> ResultSpec<T> create(Cardinality cardinality, T result) {
		return new ResultSpec<T>(cardinality, result);
	}

	@SuppressWarnings("unchecked")
	public static <T> ResultSpec<T> empty() {
		return (ResultSpec<T>) EMPTY;
	}
}
