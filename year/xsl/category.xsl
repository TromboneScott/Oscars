<?xml version="1.0" encoding="utf-8"?>
<!-- OSCARS website created by Scott McDonald -->
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
  <xsl:include href="include.xsl" />
  <xsl:template match="/category">
    <html>
      <xsl:call-template name="init" />
      <body>
        <center>
          <xsl:call-template name="header" />
          <xsl:choose>
            <xsl:when test="@name = 'all'">
              <xsl:choose>
                <xsl:when test="count($results/standings/player)=0">
                  <div id="name">Categories</div>
                  <br />
                  <i>Players' picks in each category will be loaded after all
                    ballots are received.</i>
                  <br />
                  <br />
                  <table>
                    <xsl:for-each
                      select="$definitions/column[count(nominee) &gt; 0]">
                      <tr>
                        <th>
                          <xsl:attribute name="rowspan">
                            <xsl:value-of select="(count(nominee) + 4) div 5" />
                          </xsl:attribute>
                          <xsl:value-of select="@name" />
                          <xsl:variable name="tieBreaker" select="@tieBreaker" />
                          <xsl:if test="$tieBreaker">
                            <br />
                            <span style="font-weight:normal">
                              <i>Tie Breaker: <xsl:value-of
                                  select="$tieBreaker" /></i>
                            </span>
                          </xsl:if>
                        </th>
                        <xsl:apply-templates
                          select="nominee[position() &lt;= 5]" />
                      </tr>
                      <tr>
                        <xsl:apply-templates
                          select="nominee[position() &gt; 5]" />
                      </tr>
                    </xsl:for-each>
                  </table>
                </xsl:when>
                <xsl:otherwise>
                  <div id="name">Category Results</div>
                  <br />
                  <xsl:variable name="playerCount"
                    select="count($results/standings/player)" />
                  <xsl:variable name="graphWidth" select="250" />
                  <table>
                    <thead>
                      <tr>
                        <th class="header">Category</th>
                        <th>Correct</th>
                        <th>Wrong</th>
                        <th>Graph</th>
                      </tr>
                    </thead>
                    <tbody>
                      <xsl:for-each select="$results/awards/category">
                        <xsl:variable name="name" select="@name" />
                        <tr>
                          <td class="header">
                            <a>
                              <xsl:attribute name="href">
                                <xsl:value-of select="concat('#', @name)" />
                              </xsl:attribute>
                              <xsl:value-of select="@name" />
                            </a>
                            <xsl:apply-templates
                              select="$definitions/column[@name = $name]"
                              mode="tieBreaker" />
                          </td>
                          <xsl:variable name="winners">
                            <xsl:for-each select="nominee">
                              <xsl:value-of select="concat('|', @name, '|')" />
                            </xsl:for-each>
                          </xsl:variable>
                          <xsl:choose>
                            <xsl:when test="string($winners)">
                              <xsl:variable name="correct"
                                select="count($ballots/player/category[@name = $name and contains($winners, concat('|', @nominee, '|'))])" />
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
                                    <xsl:value-of
                                      select="$graphWidth - $greenWidth" />
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
                  <xsl:for-each select="$definitions/column[nominee]">
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
                      <xsl:variable name="tieBreaker" select="@tieBreaker" />
                      <xsl:if test="$tieBreaker">
                        <xsl:value-of
                          select="concat(' (Tie Breaker: ', $tieBreaker, ')')" />
                      </xsl:if>
                      <br />
                      <xsl:for-each select="nominee">
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
                </xsl:otherwise>
              </xsl:choose>
            </xsl:when>
            <xsl:otherwise>
              <xsl:variable name="categoryName" select="@name" />
              <xsl:variable
                name="categoryDefinition"
                select="$definitions/column[@name = $categoryName]" />
              <xsl:variable
                name="awards"
                select="$results/awards/category[@name = $categoryName]" />
              <div
                id="name">
                <xsl:value-of select="$categoryName" />
              </div> Tie
              Breaker: <xsl:value-of select="$categoryDefinition/@tieBreaker" />
              <xsl:if
                test="not(normalize-space($categoryDefinition/@tieBreaker))">
                NO
              </xsl:if>
              <br /> Point Value: <xsl:apply-templates
                select="$categoryDefinition" mode="value" />
              <xsl:if
                test="count($awards/nominee) &gt; 1">
                <br />
                <br />
                <b>TIE</b> - Everyone who selected one of the
              winners in this category gets the points. </xsl:if>
              <br />
              <br />
              <xsl:apply-templates
                select="$categoryDefinition" mode="chart" />
              <br />
              <br />
              <br />
              <table>
                <thead>
                  <tr>
                    <th class="header">
                      <xsl:value-of select="$categoryName" />
                    </th>
                    <xsl:for-each select="$categoryDefinition/nominee">
                      <th>
                        <xsl:apply-templates select="$awards" mode="attribute">
                          <xsl:with-param name="nominee" select="@name" />
                        </xsl:apply-templates>
                        <xsl:apply-templates select="." mode="poster">
                          <xsl:with-param name="category" select="$categoryName" />
                        </xsl:apply-templates>
                        <br />
                        <xsl:value-of select="." />
                      </th>
                    </xsl:for-each>
                  </tr>
                </thead>
                <tbody>
                  <xsl:for-each select="$results/standings/player">
                    <xsl:sort select="@lastName" />
                    <xsl:sort select="@firstName" />
                    <xsl:variable name="player" select="." />
                    <xsl:variable name="guess"
                      select="$ballots/player[@firstName = $player/@firstName and @lastName = $player/@lastName]/category[@name = $categoryName]/@nominee" />
                    <tr>
                      <xsl:apply-templates select="$awards" mode="attribute">
                        <xsl:with-param name="nominee" select="$guess" />
                      </xsl:apply-templates>
                      <td class="header">
                        <xsl:apply-templates select="." mode="playerLink" />
                      </td>
                      <xsl:for-each select="$categoryDefinition/nominee">
                        <td id="selection">
                          <xsl:if test="@name = $guess">
                            ✔
                          </xsl:if>
                        </td>
                      </xsl:for-each>
                    </tr>
                  </xsl:for-each>
                </tbody>
                <tfoot>
                  <tr>
                    <th class="header">Total</th>
                    <xsl:for-each select="$categoryDefinition/nominee">
                      <th>
                        <xsl:apply-templates select="$awards" mode="attribute">
                          <xsl:with-param name="nominee" select="@name" />
                        </xsl:apply-templates>
                        <xsl:variable name="name" select="@name" />
                        <xsl:value-of
                          select="count($ballots/player/category[@name = $categoryName and @nominee = $name])" />
                      </th>
                    </xsl:for-each>
                  </tr>
                </tfoot>
              </table>
            </xsl:otherwise>
          </xsl:choose>
          <br />
          <a href=".." id="return">Return to Main Page</a>
          <br />
          <br />
          <xsl:call-template name="updated" />
          <xsl:call-template name="credits" />
        </center>
      </body>
    </html>
  </xsl:template>
  <xsl:template match="/definitions/column/nominee">
    <td style="text-align: center">
      <img>
        <xsl:attribute name="src">
          <xsl:value-of select="@img" />
        </xsl:attribute>
        <xsl:attribute name="alt">
          <xsl:value-of select="@name" />
        </xsl:attribute>
        <xsl:attribute name="title">
          <xsl:value-of select="@name" />
        </xsl:attribute>
      </img>
      <br />
      <xsl:value-of select="@name" />
    </td>
  </xsl:template>
  <xsl:template match="/definitions/column" mode="chart">
    <img>
      <xsl:attribute name="src">
        <xsl:value-of select="@name" />
        <xsl:variable name="category" select="@name" />
        <xsl:variable name="awards"
          select="$results/awards/category[@name = $category]" />
        <xsl:for-each select="nominee">
          <xsl:variable name="nominee" select="@name" />
          <xsl:choose>
            <xsl:when test="$awards/nominee[@name = $nominee]">
              <xsl:value-of select="'1'" />
            </xsl:when>
            <xsl:otherwise>
              <xsl:value-of select="'0'" />
            </xsl:otherwise>
          </xsl:choose>
        </xsl:for-each>
        <xsl:value-of select="'.png'" />
      </xsl:attribute>
      <xsl:attribute name="alt">
        <xsl:value-of select="@name" />
      </xsl:attribute>
      <xsl:attribute name="title">
        <xsl:value-of select="@name" />
      </xsl:attribute>
    </img>
  </xsl:template>
</xsl:stylesheet>