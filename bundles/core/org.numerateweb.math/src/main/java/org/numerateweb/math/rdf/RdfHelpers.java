package org.numerateweb.math.rdf;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;

import net.enilink.komma.core.IEntityManagerFactory;
import net.enilink.komma.core.IUnitOfWork;
import net.enilink.komma.core.KommaModule;
import net.enilink.komma.dm.IDataManager;
import net.enilink.komma.dm.IDataManagerFactory;
import net.enilink.komma.em.DecoratingEntityManagerModule;
import net.enilink.komma.em.EntityManagerFactoryModule;
import net.enilink.komma.em.util.KommaUtil;
import net.enilink.komma.em.util.UnitOfWork;
import net.enilink.komma.rdf4j.RDF4JMemoryStoreModule;

/**
 * Helper methods for working with RDF data.
 */
public class RdfHelpers {
	static KommaModule mathModule = new NWMathModule();

	public static IEntityManagerFactory createInMemoryEMFactory() {
		KommaModule kommaModule = new KommaModule();
		kommaModule.includeModule(KommaUtil.getCoreModule());
		kommaModule.includeModule(mathModule);
		Injector injector = Guice.createInjector(new RDF4JMemoryStoreModule(),
				new EntityManagerFactoryModule(kommaModule, null, new DecoratingEntityManagerModule()),
				new AbstractModule() {
					@Override
					protected void configure() {
						UnitOfWork uow = new UnitOfWork();
						uow.begin();
						bind(UnitOfWork.class).toInstance(uow);
						bind(IUnitOfWork.class).toInstance(uow);
						bind(IDataManager.class).toProvider(IDataManagerFactory.class);
					}
				});
		return injector.getInstance(IEntityManagerFactory.class);
	}
}