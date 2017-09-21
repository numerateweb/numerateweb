package org.numerateweb.math.cli;

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.stream.XMLStreamException;

import org.eclipse.core.runtime.IStatus;
import org.numerateweb.math.concepts.NWMATH;
import org.numerateweb.math.ns.Namespaces;
import org.numerateweb.math.rdf.NWMathBuilder;
import org.numerateweb.math.rdf.meta.NWMETA;
import org.numerateweb.math.search.Helper;
import org.numerateweb.math.util.stax.ParseException;
import org.numerateweb.math.xml.IOMXmlParser;
import org.numerateweb.math.xml.OMReader;

import net.enilink.komma.core.IEntityManager;
import net.enilink.komma.core.IEntityManagerFactory;
import net.enilink.komma.core.IStatement;
import net.enilink.komma.core.ITransaction;
import net.enilink.komma.core.Namespace;
import net.enilink.komma.core.URI;
import net.enilink.komma.core.URIs;
import net.enilink.komma.core.visitor.IDataAndNamespacesVisitor;
import net.enilink.komma.model.ModelUtil;
import net.enilink.vocab.rdf.RDF;

public class ConvertOMObject implements CLICommand {

	public String description() {
		return "Converts one or more OpenMath objects to RDF.";
	}

	@Override
	public String name() {
		return "convert";
	}

	public void run(String... args) {
		List<String> files = new ArrayList<>();
		String type = null;
		for (int i = 0; i < args.length; i++) {
			if ("-t".equals(args[i]) && i < args.length - 1) {
				type = args[++i];
			} else {
				files.add(args[i]);
			}
		}
		if (files.isEmpty()) {
			System.err.println("Usage: " + usage());
			return;
		}
		Map<String, URI> nsMap = new HashMap<>();
		nsMap.put("", URIs.createURI("urn:numerateweb:"));
		nsMap.put("rdf", RDF.NAMESPACE_URI);
		nsMap.put("math", NWMATH.NAMESPACE_URI);
		nsMap.put("math-meta", NWMETA.NAMESPACE_URI);
		try (IEntityManagerFactory emFactory = Helper.createInMemoryEMFactory();
				final IEntityManager em = emFactory.get()) {
			for (Map.Entry<String, URI> e : nsMap.entrySet()) {
				em.setNamespace(e.getKey(), e.getValue());
			}
			for (String file : files) {
				IStatus status = new OMReader(Arrays.<String>asList()) {
					public void parse(String fileName, IOMXmlParser parser) throws XMLStreamException, ParseException {
						ITransaction transaction = em.getTransaction();
						try {
							transaction.begin();
							parser.parse(new NWMathBuilder(em, new Namespaces(em)));
							transaction.commit();
						} finally {
							if (transaction.isActive()) {
								transaction.rollback();
							}
						}
					};
				}.readAll(URIs.createURI(Paths.get(file).toAbsolutePath().toUri().toString()),
						new CLIProgressMonitor(System.err));
				if (!status.isOK()) {
					StatusUtil.printStatus(System.err, status);
				}
			}
			type = ModelUtil.mimeType("example." + (type == null ? "ttl" : type));

			IDataAndNamespacesVisitor<Void> visitor = ModelUtil.writeData(System.out, "urn:numerateweb:", type,
					"UTF-8");
			visitor.visitBegin();
			for (Map.Entry<String, URI> e : nsMap.entrySet()) {
				visitor.visitNamespace(new Namespace(e.getKey(), e.getValue()));
			}
			for (IStatement stmt : em.match(null, null, null).mapWith(new NodeIdShortener())) {
				visitor.visitStatement(stmt);
			}
			visitor.visitEnd();
		}
	}

	@Override
	public String usage() {
		return name() + " file [file1 ...] [-t ttl|xml|nt]";
	}
}