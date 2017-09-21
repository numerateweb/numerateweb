package org.numerateweb.math.om.cd;

import java.util.Arrays;

import javax.xml.stream.XMLStreamException;

import net.enilink.komma.core.ITransaction;
import net.enilink.komma.core.URI;
import net.enilink.komma.model.IModel;
import net.enilink.komma.model.IURIConverter;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.numerateweb.math.ns.Namespaces;
import org.numerateweb.math.rdf.meta.NWMetaBuilder;
import org.numerateweb.math.util.stax.ParseException;
import org.numerateweb.math.xml.IOMXmlParser;
import org.numerateweb.math.xml.OMReader;

public class OMCDImporter extends OMReader {
	protected IModel model;

	public OMCDImporter(IModel model) {
		super(Arrays.asList("ocd"));
		this.model = model;
	}

	@Override
	protected IURIConverter getURIConverter() {
		return model.getModelSet().getURIConverter();
	}

	@Override
	public IStatus readAll(URI resourceUri, IProgressMonitor monitor) {
		try {
			model.getModelSet().getUnitOfWork().begin();
			return super.readAll(resourceUri, monitor);
		} finally {
			model.setModified(true);
			model.getModelSet().getUnitOfWork().end();
		}
	}

	@Override
	public void parse(String fileName, IOMXmlParser parser)
			throws XMLStreamException, ParseException {
		ITransaction transaction = model.getManager().getTransaction();
		try {
			model.getModelSet().getDataChangeSupport().setEnabled(null, false);
			transaction.begin();
			parser.parse(new NWMetaBuilder(model.getManager(), new Namespaces(
					model.getManager())));
			transaction.commit();
		} finally {
			if (transaction.isActive()) {
				transaction.rollback();
			}
			model.getModelSet().getDataChangeSupport().setEnabled(null, true);
		}
	}
}
