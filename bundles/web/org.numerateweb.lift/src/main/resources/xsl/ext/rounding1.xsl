<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	xmlns:om="http://www.openmath.org/OpenMath" xmlns="http://www.w3.org/1998/Math/MathML"
	version="1.0">
	<xsl:template match="om:OMS[@cd='rounding1' and @name='ceiling']">
		<xsl:choose>
			<xsl:when test="parent::om:OMA and not(preceding-sibling::*)">
				<mfenced open="&#x2308;" close="&#x2309;">
					<xsl:apply-templates select="following-sibling::*[1]" />
				</mfenced>
			</xsl:when>
			<xsl:otherwise>
				<mi>ceiling</mi>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>

	<xsl:template match="om:OMS[@cd='rounding1' and @name='floor']">
		<xsl:choose>
			<xsl:when test="parent::om:OMA and not(preceding-sibling::*)">
				<mfenced open="&#x230A;" close="&#x230B;">
					<xsl:apply-templates select="following-sibling::*[1]" />
				</mfenced>
			</xsl:when>
			<xsl:otherwise>
				<mi>floor</mi>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>
</xsl:stylesheet>