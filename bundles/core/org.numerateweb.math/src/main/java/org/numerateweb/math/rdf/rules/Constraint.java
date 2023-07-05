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
package org.numerateweb.math.rdf.rules;

import net.enilink.composition.annotations.Iri;

import net.enilink.komma.core.IEntity;
import net.enilink.komma.core.IReference;

@Iri("http://numerateweb.org/vocab/math/rules#Constraint")
public interface Constraint extends IEntity {

	@Iri("http://numerateweb.org/vocab/math/rules#expressionString")
	String getExpressionString();
	
	void setExpressionString(String value);
	
	@Iri("http://numerateweb.org/vocab/math/rules#expression")
	IReference getExpression();

	void setExpression(IReference value);

	@Iri("http://numerateweb.org/vocab/math/rules#onProperty")
	IReference getOnProperty();

	void setOnProperty(IReference property);
}