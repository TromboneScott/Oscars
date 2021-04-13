<xsl:stylesheet version="1.0"
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
  <xsl:import href="header.xsl" />
  <xsl:import href="../category/correct.xsl" />
  <xsl:template match="/categories">
    <html>
      <xsl:variable name="results"
        select="document('../results.xml')/results" />
      <xsl:call-template name="init">
        <xsl:with-param name="results" select="$results" />
      </xsl:call-template>
      <body>
        <center>
          <xsl:call-template name="header">
            <xsl:with-param name="results" select="$results" />
          </xsl:call-template>
          <div id="name">
            Category Guesses
          </div>
          <br />
          <br />
          <img usemap="#correct" >
            <xsl:attribute name="src">
              <xsl:value-of select="concat('correct_', $results/updates, '.png')" />
            </xsl:attribute>
          </img>
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
              <xsl:variable name="thisName" select="name" />
              <img>
                <xsl:attribute name="src">
                  <xsl:for-each select="$results/categories/category">
                    <xsl:if test="name = $thisName">
                      <xsl:value-of select="chart" />
                    </xsl:if>
                  </xsl:for-each>
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
