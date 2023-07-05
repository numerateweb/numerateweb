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
package org.numerateweb.math.rdf.vocab;

import java.util.List;

import net.enilink.composition.annotations.Iri;
import net.enilink.composition.properties.annotations.Type;

import net.enilink.komma.core.IReference;

/** 
 * 
 * @generated 
 */
@Iri("http://numerateweb.org/vocab/math#Error")
public interface Error extends Compound {
	/** 
	 * 
	 * @generated 
	 */
	@Iri("http://numerateweb.org/vocab/math#arguments")
	@Type("http://www.w3.org/1999/02/22-rdf-syntax-ns#List")
	List<IReference> getArguments();
	/** 
	 * 
	 * @generated 
	 */
	void setArguments(List<IReference> arguments);

	/** 
	 * 
	 * @generated 
	 */
	@Iri("http://numerateweb.org/vocab/math#symbol")
	Symbol getSymbol();
	/** 
	 * 
	 * @generated 
	 */
	void setSymbol(Symbol symbol);

}
