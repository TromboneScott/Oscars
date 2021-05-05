<xsl:stylesheet version="1.0"
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
  <xsl:template name="init">
    <xsl:param name="results" />
    <head>
      <link rel="stylesheet" type="text/css" href="../../oscars.css" />
      <meta http-equiv="cache-control" content="no-cache" />
      <meta http-equiv="expires" content="0" />
      <meta http-equiv="pragma" content="no-cache" />
      <title>
        <xsl:value-of select="$results/title" />
      </title>
    </head>
  </xsl:template>
  <xsl:template name="header">
    <xsl:param name="results" />
    <table id="header">
      <tr>
        <td rowspan="2">
          <img src="../../trophy.png" id="trophy" />
        </td>
        <th>
          <xsl:value-of select="$results/title" />
        </th>
        <td rowspan="2">
          <img src="../../trophy.png" id="trophy" />
        </td>
      </tr>
      <tr>
        <td>(Unofficial Results)</td>
      </tr>
    </table>
    <br />
    <i>Tip: Use ctrl-alt-shift when clicking reload to force page to refresh.</i>
    <br />
    <br />
    <br />
  </xsl:template>
  <xsl:template name="string-replace-all">
    <xsl:param name="text" />
    <xsl:param name="replace" />
    <xsl:param name="by" />
    <xsl:choose>
      <xsl:when test="contains($text, $replace)">
        <xsl:value-of select="substring-before($text,$replace)" />
        <xsl:value-of select="$by" />
        <xsl:call-template name="string-replace-all">
          <xsl:with-param name="text"
            select="substring-after($text,$replace)" />
          <xsl:with-param name="replace" select="$replace" />
          <xsl:with-param name="by" select="$by" />
        </xsl:call-template>
      </xsl:when>
      <xsl:otherwise>
        <xsl:value-of select="$text" />
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>
  <xsl:template name="category-xml-file">
    <xsl:call-template name="string-replace-all">
      <xsl:with-param name="text" select="concat('../category/', name, '.xml')" />
      <xsl:with-param name="replace" select="' '" />
      <xsl:with-param name="by" select="'%20'" />
    </xsl:call-template>
  </xsl:template>
</xsl:stylesheet>
