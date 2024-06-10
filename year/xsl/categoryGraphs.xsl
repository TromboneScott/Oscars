<?xml version="1.0" encoding="utf-8"?>
<!-- OSCARS website created by Scott McDonald -->
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
  <xsl:include href="include.xsl" />
  <xsl:template match="/all">
    <html>
      <xsl:call-template name="init" />
      <xsl:variable name="categories" select="." />
      <body>
        <center>
          <xsl:call-template name="header" />
          <div id="name">
            Category Results
          </div>
          <br />
          <xsl:variable name="playerCount"
            select="count($results/players/player)" />
          <xsl:variable name="graphWidth" select="250" />
          <table>
            <thead>
              <tr>
                <th class="header">
                  Category
                </th>
                <th>
                  Correct
                </th>
                <th>
                  Wrong
                </th>
                <th>
                  Graph
                </th>
              </tr>
            </thead>
            <tbody>
              <xsl:for-each select="$results/categories/category">
                <tr>
                  <td class="header">
                    <a>
                      <xsl:attribute name="href">
                        <xsl:value-of select="concat('#', @name)" />
                      </xsl:attribute>
                      <xsl:value-of select="@name" />
                    </a>
                    <xsl:variable name="name" select="@name" />
                    <xsl:apply-templates
                      select="$definitions/category[@name = $name]"
                      mode="tieBreaker" />
                  </td>
                  <xsl:variable name="winners">
                    <xsl:for-each select="winners/nominee">
                      <xsl:value-of select="concat('|', @name, '|')" />
                    </xsl:for-each>
                  </xsl:variable>
                  <xsl:choose>
                    <xsl:when test="string($winners)">
                      <xsl:variable name="name" select="@name" />
                      <xsl:variable name="correct"
                        select="count($categories/category[@name = $name]/player[contains($winners, concat('|', @guess, '|'))])" />
                      <td class="correct">
                        <center>
                          <xsl:value-of select="$correct" />
                        </center>
                      </td>
                      <td class="incorrect">
                        <center>
                          <xsl:value-of select="$playerCount - $correct" />
                        </center>
                      </td>
                      <td>
                        <xsl:variable name="greenWidth"
                          select="$graphWidth * $correct div $playerCount" />
                        <img src="../../bar_green.bmp" height="15">
                          <xsl:attribute name="width">
                            <xsl:value-of select="$greenWidth" />
                          </xsl:attribute>
                        </img>
                        <img src="../../bar_red.bmp" height="15">
                          <xsl:attribute name="width">
                            <xsl:value-of select="$graphWidth - $greenWidth" />
                          </xsl:attribute>
                        </img>
                      </td>
                    </xsl:when>
                    <xsl:otherwise>
                      <td class="unannounced" />
                      <td class="unannounced" />
                      <td>
                        <img src="../../bar_grey.bmp" height="15">
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
          <xsl:for-each select="$results/categories/category">
            <br />
            <br />
            <br />
            <a>
              <xsl:attribute name="id">
                <xsl:value-of select="@name" />
              </xsl:attribute>
              <xsl:attribute name="href">
                <xsl:value-of select="concat(@name, '.xml')" />
              </xsl:attribute>
              <b>
                <xsl:value-of select="@name" />
              </b>
              <xsl:variable name="categoryName" select="@name" />
              <xsl:variable name="tieBreaker"
                select="$definitions/category[@name = $categoryName]/@tieBreaker" />
              <xsl:if test="$tieBreaker">
                <xsl:value-of
                  select="concat(' (Tie Breaker: ', $tieBreaker, ')')" />
              </xsl:if>
              <br />
              <xsl:for-each
                select="$definitions/category[@name = $categoryName]/nominee">
                <xsl:if test="(position() - 1) mod 5 = 0">
                  <br />
                </xsl:if>
                <xsl:apply-templates select="." mode="poster">
                  <xsl:with-param name="category" select="$categoryName" />
                </xsl:apply-templates>
              </xsl:for-each>
              <br />
              <xsl:apply-templates select="." mode="chart" />
            </a>
            <br />
          </xsl:for-each>
          <br />
          <br />
          <a href=".." id="return">Return to Main Page</a>
          <br />
          <br />
          <xsl:call-template name="updated" />
        </center>
      </body>
    </html>
  </xsl:template>
</xsl:stylesheet>