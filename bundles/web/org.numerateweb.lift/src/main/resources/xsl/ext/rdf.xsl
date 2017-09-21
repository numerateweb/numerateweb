<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	xmlns:om="http://www.openmath.org/OpenMath" xmlns="http://www.w3.org/1998/Math/MathML"
	version="1.0">

	<!-- set1.intersect(rdf.valueset(...), rdf.resourceset(...)) -->
	<xsl:template
		match="om:OMS[(@cd='set1' or @cd='multiset1') and @name='intersect' and
	./following-sibling::om:OMA/om:OMS[@cd='rdf' and @name='valueset']]">
		<xsl:param name="p" />
		<xsl:call-template name="infix">
			<xsl:with-param name="mo"></xsl:with-param>
			<xsl:with-param name="p" select="$p" />
			<xsl:with-param name="this-p" select="2" />
		</xsl:call-template>
	</xsl:template>

	<xsl:template match="om:OMS[@cd='rdf' and @name='resourceset']">
		<mrow>
			<xsl:choose>
				<!-- set1.intersect(rdf.valueset(...), rdf.resourceset(...)) -->
				<xsl:when
					test="parent::om:OMA/preceding-sibling::om:OMS[position() = 1 and @cd='set1' and @name='intersect'] and 
				parent::*/preceding-sibling::om:OMA/om:OMS[@cd='rdf' and @name='valueset']"></xsl:when>
				<xsl:otherwise>
					<mo lspace="0" rspace="0">@@</mo>
				</xsl:otherwise>
			</xsl:choose>
			<xsl:for-each select="following-sibling::*">
				<mfenced open="[" close="]">
					<xsl:apply-templates select="." />
				</mfenced>
			</xsl:for-each>
		</mrow>
	</xsl:template>

	<xsl:template match="om:OMS[@cd='rdf' and @name='resource']">
		<mrow>
			<mo lspace="0" rspace="0">@</mo>
			<mfenced>
				<xsl:apply-templates select="following-sibling::*" />
			</mfenced>
		</mrow>
	</xsl:template>

	<xsl:template
		match="om:OMS[@cd='rdf' and (@name='value' or @name='valueset')]">
		<xsl:variable name="mo">
			<mrow>
				<mo lspace="0" rspace="0">
					<xsl:choose>
						<xsl:when test="@name='valueset'">@@</xsl:when>
						<xsl:when test="@name='value'">@</xsl:when>
						<xsl:otherwise></xsl:otherwise>
					</xsl:choose>
				</mo>
				<xsl:apply-templates select="following-sibling::*[1]" />
			</mrow>
		</xsl:variable>
		<xsl:choose>
			<xsl:when
				test="parent::om:OMA and not(preceding-sibling::*) and following-sibling::*[position() > 1]">
				<mrow>
					<xsl:copy-of select="$mo" />
					<mo>&#x2061;</mo>
					<mfenced>
						<xsl:for-each select="following-sibling::*[position() > 1]">
							<xsl:apply-templates select="." />
							<xsl:if test="position() &lt; last()">
								<mo>,</mo>
							</xsl:if>
						</xsl:for-each>
					</mfenced>
				</mrow>
			</xsl:when>
			<xsl:otherwise>
				<xsl:copy-of select="$mo" />
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>
</xsl:stylesheet>