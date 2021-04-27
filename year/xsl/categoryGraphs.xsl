<xsl:stylesheet version="1.0"
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
  <xsl:import href="header.xsl" />
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
            Category Results
          </div>
          <br />
          <xsl:variable name="playerCount" select="$results/players/count" />
          <xsl:variable name="graphWidth" select="250" />
          <table>
            <thead>
              <tr>
                <th class="header">
                  Category
                </th>
                <th class="header">
                  Correct
                </th>
                <th class="header">
                  Wrong
                </th>
                <th class="header">
                  Graph
                </th>
              </tr>
            </thead>
            <tbody>
              <xsl:for-each select="$results/categories/category">
                <tr>
                  <td>
                      <a>
                        <xsl:attribute name="href">
                          <xsl:value-of select="concat('#', name)" />
                        </xsl:attribute>
                        <xsl:value-of select="name" />
                      </a>
                  </td>
                  <xsl:choose>
                    <xsl:when test="string(correct)">
                      <td class="correct">
                        <center>
                          <xsl:value-of select="correct" />
                        </center>
                      </td>
                      <td class="incorrect">
                        <center>
                          <xsl:value-of select="$playerCount - correct" />
                        </center>
                      </td>
                      <td>
                        <xsl:variable name="greenWidth" select="$graphWidth * correct div $playerCount" />
                        <img src="../../bar_green.bmp" height="15" >
                          <xsl:attribute name="width">
                            <xsl:value-of select="$greenWidth" />
                          </xsl:attribute>
                        </img>
                        <img src="../../bar_red.bmp" height="15" >
                          <xsl:attribute name="width">
                            <xsl:value-of select="$graphWidth - $greenWidth" />
                          </xsl:attribute>
                        </img>
                      </td>
                    </xsl:when>
                    <xsl:otherwise>
                      <td class="unannounced"/>
                      <td class="unannounced"/>
                      <td>
                        <img src="../../bar_grey.bmp" height="15" >
                          <xsl:attribute name="width">
                            <xsl:value-of select="$graphWidth" />
                          </xsl:attribute>
                        </img>
                      </td>
                    </xsl:otherwise>
                  </xsl:choose>
                </tr>
              </xsl:for-each>
            </tbody>
          </table>
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
