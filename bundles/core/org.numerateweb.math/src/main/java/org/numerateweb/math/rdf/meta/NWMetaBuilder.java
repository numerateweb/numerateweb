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
package org.numerateweb.math.rdf.meta;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.numerateweb.math.model.OMObject;
import org.numerateweb.math.model.OMObject.Type;
import org.numerateweb.math.model.OMObjectBuilderBase;
import org.numerateweb.math.model.OMObjectParser;
import org.numerateweb.math.ns.INamespaces;
import org.numerateweb.math.om.cd.META;
import org.numerateweb.math.rdf.NWMathBuilder;
import org.numerateweb.math.rdf.NWMathBuilderBase;
import org.numerateweb.math.rdf.vocab.NWMATH;

import net.enilink.vocab.rdf.RDF;
import net.enilink.vocab.rdfs.RDFS;
import net.enilink.komma.core.IEntityManager;
import net.enilink.komma.core.IReference;
import net.enilink.komma.core.Statement;
import net.enilink.komma.core.URI;
import net.enilink.komma.core.URIs;

/**
 * Extends {@link NWMathBuilder} with transformations for the representation of
 * OpenMath Content Dictionaries as OWL ontologies.
 */
public class NWMetaBuilder extends NWMathBuilderBase<IReference> {
	public NWMetaBuilder(IEntityManager em, INamespaces ns) {
		super(em, ns);
	}

	protected void convertAttributes(URI target, Map<URI, URI> mapping,
			Map<URI, List<String>> attributes) {
		IEntityManager em = getEntityManager();
		for (Map.Entry<URI, List<String>> entry : attributes.entrySet()) {
			URI property = mapping.get(entry.getKey());
			if (property != null) {
				for (String value : entry.getValue()) {
					em.add(new Statement(target, property, value));
				}
			}
		}
	}

	protected void convertDefinition(Context context, URI cdUri,
			java.lang.Object[] args) {
		IEntityManager em = getEntityManager();

		List<URI> symbols = Arrays.asList(META.NAME, META.ROLE,
				META.DESCRIPTION, META.CDCOMMENT, META.CMP);
		Map<URI, List<String>> attributes = new HashMap<>();
		for (int i = 1; i < args.length; i++) {
			extract(args[i], symbols, attributes);
		}

		String name = getSingle(META.NAME, attributes);
		URI defUri = cdUri.appendLocalPart(name);
		Statement defBy = new Statement(defUri, RDFS.PROPERTY_ISDEFINEDBY,
				cdUri);
		if (em.hasMatch(defBy.getSubject(), defBy.getPredicate(),
				defBy.getObject())) {
			// do not import the definition if it already exists
			return;
		}
		em.add(defBy);

		String role = getSingle(META.ROLE, attributes);
		URI type = NWMATH.TYPE_SYMBOL;
		if (role != null) {
			try {
				Field field = NWMETA.class.getField("TYPE_"
						+ role.replace("-", "").toUpperCase() + "SYMBOL");
				type = (URI) field.get(null);
			} catch (Exception e) {
				// ignore
			}
		}
		em.add(new Statement(defUri, RDF.PROPERTY_TYPE, type));

		for (int i = 1; i < args.length; i++) {
			java.lang.Object arg = args[i];
			if (isOMA(arg, META.FMP)) {
				IReference mathObj = toRdf((OMObject) ((OMObject) arg)
						.getArgs()[1]);
				em.add(new Statement(defUri, NWMETA.PROPERTY_FORMALPROPERTY,
						mathObj));
			} else if (isOMA(arg, META.EXAMPLE)) {
				em.add(new Statement(defUri, NWMETA.PROPERTY_EXAMPLE,
						toRdf((OMObject) arg)));
			}
		}

		Map<URI, URI> mapping = new HashMap<>();
		mapping.put(META.DESCRIPTION, NWMETA.PROPERTY_DESCRIPTION);
		mapping.put(META.CMP, NWMETA.PROPERTY_COMMENTEDPROPERTY);
		mapping.put(META.CDCOMMENT, RDFS.PROPERTY_COMMENT);
		convertAttributes(defUri, mapping, attributes);
	}

	protected void extract(java.lang.Object obj, List<URI> symbols,
			Map<URI, List<String>> attributes) {
		if (obj instanceof OMObject && ((OMObject) obj).getType() == Type.OMA) {
			java.lang.Object[] nodeArgs = ((OMObject) obj).getArgs();
			Object symbol = ((OMObject) nodeArgs[0]).getArgs()[0];
			for (URI s : symbols) {
				if (s.equals(symbol)) {
					List<String> values = attributes.get(s);
					if (values == null) {
						values = new ArrayList<String>(1);
					}
					values.add(((OMObject) nodeArgs[1]).getArgs()[0].toString());
					attributes.put(s, values);
				}
			}
		}
	}

	@Override
	protected BuilderFactory getBuilderFactory(URI symbol) {
		if (META.CD.equals(symbol)) {
			return new BuilderFactory() {
				@Override
				public <E> SeqBuilder<E> create(
						final NWMathBuilderBase<E> parent, final URI symbol) {
					return new OMObjectBuilderBase.OMObjectSeqBuilder<E>() {
						List<OMObject> args = new ArrayList<OMObject>();

						@Override
						protected SeqBuilder<E> build(OMObject obj) {
							args.add(obj);
							return this;
						}

						@Override
						public E end() {
							Map<URI, List<String>> attributes = new HashMap<>();
							List<URI> symbols = Arrays.asList(META.CDNAME,
									META.CDBASE, META.CDURL, META.DESCRIPTION,
									META.CDCOMMENT, META.CDDATE,
									META.CDVERSION, META.CDREVISION,
									META.CDREVIEWDATE, META.CDSTATUS);
							// extract attributes first
							for (OMObject arg : args) {
								extract(arg, symbols, attributes);
							}

							String base = getSingle(META.CDBASE, attributes);
							if (base == null) {
								base = "http://www.openmath.org/cd";
							}
							String name = getSingle(META.CDNAME, attributes);
							URI cdUri = URIs.createURI(base + "/" + name);

							// convert cd definitions
							for (OMObject arg : args) {
								if (isOMA(arg, META.CDDEFINITION)) {
									convertDefinition(context, cdUri,
											((OMObject) arg).getArgs());
								}
							}

							Map<URI, URI> mapping = new HashMap<>();
							mapping.put(META.DESCRIPTION,
									NWMETA.PROPERTY_DESCRIPTION);
							mapping.put(META.CDCOMMENT, RDFS.PROPERTY_COMMENT);
							convertAttributes(cdUri, mapping, attributes);
							return parent.build(getEntityManager().createNamed(
									cdUri, NWMETA.TYPE_LIBRARY));
						}

					};
				}
			};
		}
		return super.getBuilderFactory(symbol);
	}

	protected String getSingle(URI key, Map<URI, List<String>> attributes) {
		List<String> values = attributes.get(key);
		return values != null && !values.isEmpty() ? values.iterator().next()
				: null;
	}

	protected boolean isOMA(java.lang.Object obj, URI symbol) {
		if (obj instanceof OMObject && ((OMObject) obj).getType() == Type.OMA) {
			java.lang.Object[] nodeArgs = ((OMObject) obj).getArgs();
			return nodeArgs[0] instanceof OMObject
					&& symbol.equals(((OMObject) nodeArgs[0]).getArgs()[0]);
		}
		return false;
	}

	protected IReference toRdf(OMObject obj) {
		return new OMObjectParser().parse(obj, new NWMathBuilder(getContext()));
	}
}