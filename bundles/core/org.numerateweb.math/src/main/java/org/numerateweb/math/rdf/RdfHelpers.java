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
package org.numerateweb.math.rdf;

import com.google.inject.name.Names;
import net.enilink.komma.core.*;
import net.enilink.vocab.rdf.RDF;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryException;
import org.eclipse.rdf4j.repository.sail.SailRepository;
import org.eclipse.rdf4j.sail.memory.MemoryStore;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Provides;
import com.google.inject.Singleton;

import net.enilink.komma.dm.IDataManager;
import net.enilink.komma.dm.IDataManagerFactory;
import net.enilink.komma.em.DecoratingEntityManagerModule;
import net.enilink.komma.em.EntityManagerFactoryModule;
import net.enilink.komma.em.util.KommaUtil;
import net.enilink.komma.em.util.UnitOfWork;
import net.enilink.komma.rdf4j.RDF4JModule;

import java.util.List;
import java.util.function.Consumer;

/**
 * Helper methods for working with RDF data.
 */
public class RdfHelpers {
	static KommaModule mathModule = new NWMathModule();

	protected static Repository createRepository() throws RepositoryException {
		MemoryStore store = new MemoryStore();
		SailRepository repository = new SailRepository(store);
		repository.init();
		return repository;
	}

	public static IEntityManagerFactory createInMemoryEMFactory() {
		KommaModule kommaModule = new KommaModule();
		kommaModule.includeModule(KommaUtil.getCoreModule());
		kommaModule.includeModule(mathModule);
		Injector injector = Guice.createInjector(new RDF4JModule(),
				new EntityManagerFactoryModule(kommaModule, null, new DecoratingEntityManagerModule()),
				new AbstractModule() {
					@Override
					protected void configure() {
						UnitOfWork uow = new UnitOfWork();
						uow.begin();
						bind(UnitOfWork.class).toInstance(uow);
						bind(IUnitOfWork.class).toInstance(uow);
						bind(IDataManager.class).annotatedWith(Names.named("unmanaged"))
								.toProvider(IDataManagerFactory.class);
					}

					@Singleton
					@Provides
					protected Repository provideRepository() {
						try {
							return createRepository();
						} catch (RepositoryException e) {
							throw new KommaException("Unable to create repository.", e);
						}
					}
				});
		return injector.getInstance(IEntityManagerFactory.class);
	}
}