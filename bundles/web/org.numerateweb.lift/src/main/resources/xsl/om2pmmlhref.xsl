<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	xmlns:om="http://www.openmath.org/OpenMath" xmlns="http://www.w3.org/1998/Math/MathML"
	xmlns:mml="http://www.w3.org/1998/Math/MathML" exclude-result-prefixes="mml"
	xmlns:exsl="http://exslt.org/common" extension-element-prefixes="exsl"
	version="1.0">
	<!-- This stylesheet translates embedded href attributes to MathML. -->
	<xsl:output method="xml" indent="yes" />

	<xsl:import href="om2pmml.xsl" />

	<xsl:template match="*" mode="href">
		<xsl:param name="href" />
		<xsl:copy>
			<xsl:copy-of select="@*" />
			<xsl:apply-templates mode="href">
				<xsl:with-param name="href" select="$href" />
			</xsl:apply-templates>
		</xsl:copy>
	</xsl:template>

	<xsl:template
		match="*[(self::mml:mfrac | self::mml:msqrt | self::mml:mroot | self::mml:mo) and not(@href)]"
		mode="href">
		<xsl:param name="href" />
		<xsl:copy>
			<xsl:copy-of select="@*" />
			<xsl:attribute name="href"><xsl:value-of select="$href" /></xsl:attribute>
			<xsl:copy-of select="child::node()" />
		</xsl:copy>
	</xsl:template>

	<xsl:template match="om:OMS[@href]">
		<xsl:variable name="mathml">
			<xsl:apply-imports />
		</xsl:variable>
		<xsl:apply-templates mode="href" select="exsl:node-set($mathml)">
			<xsl:with-param name="href" select="@href" />
		</xsl:apply-templates>
	</xsl:template>
</xsl:stylesheet>