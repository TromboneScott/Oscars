<xsl:stylesheet version="1.0"
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
  <xsl:import href="../category/correct.xsl" />
  <xsl:template match="/categories">
    <html>
      <xsl:variable name="results"
        select="document('../results.xml')/results" />
      <head>
        <link rel="stylesheet" type="text/css" href="../oscars.css" />
        <title>
          <xsl:value-of select="$results/title" />
        </title>
      </head>
      <body>
        <center>
          <table id="header">
            <tr>
              <td rowspan="2">
                <img src="../trophy.png" id="trophy" />
              </td>
              <th>
                <xsl:value-of select="$results/title" />
              </th>
              <td rowspan="2">
                <img src="../trophy.png" id="trophy" />
              </td>
            </tr>
            <tr>
              <td>(Unofficial Results)</td>
            </tr>
          </table>
          <br />
          <br />
          <div id="name">
            Category Guesses
          </div>
          <br />
          <br />
          <img src="correct.png" usemap="#correct" />
          <xsl:apply-imports />
          <br />
          <br />
          <xsl:for-each select="category">
            <br />
            <br />
            <a>
              <xsl:attribute name="id">
                <xsl:value-of select="name" />
              </xsl:attribute>
              <xsl:attribute name="href">
                <xsl:value-of select="concat(name, '.xml')" />
              </xsl:attribute>
              <b>
                <xsl:value-of select="name" />
              </b>
              <xsl:if test="tieBreaker != ''">
                <xsl:value-of
                  select="concat(' (Tie Breaker: ', tieBreaker, ')')" />
              </xsl:if>
              <br />
              <br />
              <img>
                <xsl:attribute name="src">
                  <xsl:value-of select="concat(name, '.png')" />
                </xsl:attribute>
              </img>
            </a>
            <br />
          </xsl:for-each>
          <br />
          <br />
          <a href="../index.html" id="return">Return to Main Page</a>
          <br />
          <br />
          <div id="date">
            Last updated:
            <xsl:value-of select="$results/updated" />
          </div>
        </center>
      </body>
    </html>
  </xsl:template>
</xsl:stylesheet>
