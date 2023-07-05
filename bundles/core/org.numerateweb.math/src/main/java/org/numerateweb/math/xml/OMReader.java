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
package org.numerateweb.math.xml;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.zip.GZIPInputStream;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import net.enilink.komma.core.URI;
import net.enilink.komma.model.IURIConverter;

import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.ArchiveInputStream;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.archivers.zip.ZipArchiveInputStream;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.Status;
import org.numerateweb.math.model.OMObjectBuilder;
import org.numerateweb.math.om.cd.OMCDParser;
import org.numerateweb.math.util.stax.ParseException;

public class OMReader {
	Collection<String> extensions;

	public OMReader(Collection<String> extensions) {
		this.extensions = extensions;
	}

	protected IURIConverter getURIConverter() {
		return null;
	}

	protected InputStream inputStreamForFile(URI resourceUri)
			throws IOException {
		String testPath = resourceUri.toString().toLowerCase();
		InputStream in;
		IURIConverter uriConverter = getURIConverter();
		if (uriConverter == null) {
			try {
				in = new java.net.URI(resourceUri.toString()).toURL()
						.openStream();
			} catch (URISyntaxException e) {
				throw new IOException("Unable to open file: " + resourceUri);
			}
		} else {
			in = uriConverter.createInputStream(resourceUri);
		}
		if (testPath.endsWith(".gz")) {
			in = new GZIPInputStream(in);
			if (testPath.endsWith(".tar.gz")) {
				in = new TarArchiveInputStream(in);
			}
		} else if (testPath.endsWith(".zip")) {
			in = new ZipArchiveInputStream(in);
		}
		return in;
	}

	public void parse(String fileName, IOMXmlParser parser)
			throws XMLStreamException, ParseException {
		parser.parse(new OMObjectBuilder());
	}

	public IStatus readAll(URI resourceUri, IProgressMonitor monitor) {
		String fileName = (resourceUri.isFile() ? resourceUri.toFileString()
				: resourceUri.toString());
		List<String> errors = new ArrayList<String>();
		InputStream in = null;
		try {
			in = inputStreamForFile(resourceUri);
			if (in instanceof ArchiveInputStream) {
				Set<String> entries = new HashSet<>();
				ArchiveEntry entry;
				while ((entry = ((ArchiveInputStream) in).getNextEntry()) != null) {
					if (!entry.isDirectory()) {
						String name = entry.getName().toLowerCase();
						int index = name.lastIndexOf('.');
						if (index >= 0
								&& extensions.contains(name
										.substring(index + 1))) {
							entries.add(entry.getName());
						}
					}
				}
				in.close();
				monitor.beginTask("Reading files", entries.size());
				in = inputStreamForFile(resourceUri);
				while ((entry = ((ArchiveInputStream) in).getNextEntry()) != null) {
					if (!entry.isDirectory()
							&& entries.contains(entry.getName())) {
						monitor.subTask("Reading " + entry.getName());
						readSingle(entry.getName(), in, errors);
						monitor.worked(1);
						if (monitor.isCanceled()) {
							break;
						}
					}
				}
			} else {
				monitor.beginTask("Reading file " + fileName, 1);
				readSingle(fileName, in, errors);
			}
			monitor.done();
			// explicitly close input stream
			in.close();
		} catch (IOException e) {
			errors.add("Unable to read file \"" + fileName + "\": "
					+ e.getMessage());
		} finally {
			if (in != null) {
				try {
					in.close();
				} catch (IOException e) {
					// ignore
				}
			}
		}
		if (!errors.isEmpty()) {
			final MultiStatus status = new MultiStatus("org.numerateweb.math",
					0, "Failed to read one or more content dictionaries", null);
			for (String error : errors) {
				status.add(new Status(Status.ERROR, "org.numerateweb.math",
						error));
			}
			return status;
		}
		return Status.OK_STATUS;
	}

	protected void readSingle(String fileName, InputStream in,
			List<String> errors) {
		try {
			XMLInputFactory factory = XMLInputFactory.newInstance();
			XMLStreamReader reader = factory
					.createXMLStreamReader(new InputStreamReader(in) {
						@Override
						public void close() throws IOException {
							// do not close stream
						}
					});
			IOMXmlParser parser = fileName.toLowerCase().endsWith(".ocd") ? new OMCDParser(
					reader) : new OMXmlParser(reader);
			parse(fileName, parser);
		} catch (XMLStreamException xse) {
			errors.add("File \"" + fileName + "\": " + xse.getMessage());
		} catch (ParseException pe) {
			errors.add("File \"" + fileName + "\": " + pe);
		} catch (Exception e) {
			errors.add("Processing file \"" + fileName + "\" failed: " + e);
		}
	}
}