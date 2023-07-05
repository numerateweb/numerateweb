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
package org.numerateweb.math.om.cd;

import javax.xml.namespace.QName;

public interface OMCD {
	final String NS = "http://www.openmath.org/OpenMathCD";

	final QName CD = new QName(NS, "CD");
	final QName CDBASE = new QName(NS, "CDBase");
	final QName CDCOMMENT = new QName(NS, "CDComment");
	final QName CDDATE = new QName(NS, "CDDate");
	final QName CDDEFINITION = new QName(NS, "CDDefinition");
	final QName CDNAME = new QName(NS, "CDName");
	final QName CDREVIEWDATE = new QName(NS, "CDReviewDate");
	final QName CDREVISION = new QName(NS, "CDRevision");
	final QName CDSTATUS = new QName(NS, "CDStatus");
	final QName CDURL = new QName(NS, "CDURL");
	final QName CDUSES = new QName(NS, "CDUses");
	final QName CDVERSION = new QName(NS, "CDVersion");
	final QName DESCRIPTION = new QName(NS, "Description");

	final QName NAME = new QName(NS, "Name");
	final QName ROLE = new QName(NS, "Role");
	final QName CMP = new QName(NS, "CMP");
	final QName FMP = new QName(NS, "FMP");
	final QName EXAMPLE = new QName(NS, "Example");
}
