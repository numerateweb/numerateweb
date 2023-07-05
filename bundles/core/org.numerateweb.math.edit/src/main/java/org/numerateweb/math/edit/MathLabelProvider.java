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
package org.numerateweb.math.edit;

import org.numerateweb.math.ns.INamespaces;
import org.numerateweb.math.ns.Namespaces;
import org.numerateweb.math.popcorn.PopcornBuilder;
import org.numerateweb.math.rdf.NWMathParser;
import org.numerateweb.math.rdf.rules.Constraint;

import net.enilink.komma.edit.provider.IItemLabelProvider;
import net.enilink.komma.model.ModelUtil;
import net.enilink.komma.core.IEntity;
import net.enilink.komma.core.IReference;

public class MathLabelProvider implements IItemLabelProvider {

	@Override
	public String getText(Object object) {
		if (object instanceof Constraint) {
			Constraint constraint = (Constraint) object;
			IReference p = constraint.getOnProperty();
			String expr = renderMath(constraint.getExpression());
			if (p != null) {
				return ModelUtil.getLabel(p) + " = "
						+ (expr == null ? "?" : expr);
			}
			return expr;
		}
		if (object instanceof org.numerateweb.math.rdf.vocab.Object) {
			return renderMath((org.numerateweb.math.rdf.vocab.Object) object);
		}
		return ModelUtil.getLabel(object);
	}

	protected String renderMath(IReference object) {
		if (object == null) {
			return null;
		}
		INamespaces ns = new Namespaces(((IEntity) object).getEntityManager());
		return new NWMathParser(ns).resolveURIs(false)
				.parse(object, new PopcornBuilder(ns)).toString();
	}

	@Override
	public Object getImage(Object object) {
		return MathEditPlugin.INSTANCE.getBundleResourceLocator().getImage(
				"full/obj16/pi.png");
	}

}
