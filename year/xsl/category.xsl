<?xml version="1.0" encoding="utf-8"?>
<!-- OSCARS website created by Scott McDonald -->
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
  <xsl:include href="include.xsl" />
  <xsl:template match="/category">
    <html>
      <xsl:call-template name="header" />
      <body>
        <center>
          <xsl:choose>
            <xsl:when test="@name = 'all'">
              <xsl:choose>
                <xsl:when test="count($results/standings/player) = 0">
                  <div id="name">Categories</div>
                  <br />
                  <i>Players' picks in each category will be loaded after all
                    ballots are received.</i>
                  <br />
                  <br />
                  <table style="border-collapse: collapse">
                    <xsl:for-each
                      select="$definitions/column[count(nominee) > 0]">
                      <tr>
                        <xsl:attribute name="style">
                          <xsl:if test="count(nominee) &lt;= 5">
                            <xsl:value-of
                              select="'border-bottom: 3pt solid black'" />
                          </xsl:if>
                        </xsl:attribute>
                        <th style="border-right: 3pt solid black">
                          <xsl:attribute name="rowspan">
                            <xsl:value-of select="(count(nominee) + 4) div 5" />
                          </xsl:attribute>
                          <xsl:value-of select="@name" />
                          <xsl:if test="@tieBreaker">
                            <br />
                            <span style="font-weight: normal">
                              <i>Tie Breaker: <xsl:value-of
                                  select="@tieBreaker" /></i>
                            </span>
                          </xsl:if>
                        </th>
                        <xsl:apply-templates
                          select="nominee[position() &lt;= 5]">
                          <xsl:with-param name="category" select="@name" />
                        </xsl:apply-templates>
                      </tr>
                      <tr style="border-bottom: 3pt solid black">
                        <xsl:apply-templates select="nominee[position() > 5]">
                          <xsl:with-param name="category" select="@name" />
                        </xsl:apply-templates>
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
                            <a href="#{@name}">
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
                                <img src="/bar_green.bmp" height="15"
                                  width="{$greenWidth}" />
                                <img src="/bar_red.bmp" height="15">
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
                                <img src="/bar_grey.bmp" height="15"
                                  width="{$graphWidth}" />
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
                    <table>
                      <tr>
                        <th>
                          <br />
                          <a id="{@name}" href="{@name}.xml">
                            <xsl:value-of select="@name" />
                          </a>
                          <xsl:if test="@tieBreaker">
                            <br />
                            <span style="font-weight: normal">
                              <i>Tie Breaker: <xsl:value-of select="@tieBreaker" /></i>
                            </span>
                          </xsl:if>
                          <br />
                          <br />
                        </th>
                      </tr>
                      <tr>
                        <td id="rank">
                          <a id="{@name}" href="{@name}.xml">
                            <xsl:apply-templates select="nominee" mode="poster">
                              <xsl:with-param name="category"
                                select="current()/@name" />
                              <xsl:with-param name="width"
                                select="500 div count(current()/nominee)" />
                            </xsl:apply-templates>
                            <br />
                            <xsl:apply-templates select="." mode="chart">
                              <xsl:with-param name="border" select="0" />
                            </xsl:apply-templates>
                          </a>
                        </td>
                      </tr>
                    </table>
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
                test="count($awards/nominee) > 1">
                <br />
                <br />
                <b>TIE</b> - Everyone who selected one of the
              winners in this category gets the points. </xsl:if>
              <br />
              <br />
              <xsl:apply-templates
                select="$categoryDefinition" mode="chart">
                <xsl:with-param name="border" select="3" />
              </xsl:apply-templates>
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
                          <xsl:with-param name="width" select="50" />
                        </xsl:apply-templates>
                        <br />
                        <xsl:value-of select="." />
                      </th>
                    </xsl:for-each>
                  </tr>
                </thead>
                <tbody>
                  <xsl:for-each select="$results/standings/player">
                    <xsl:sort
                      select="translate(@lastName, $lowercase, $uppercase)" />
                    <xsl:sort
                      select="translate(@firstName, $lowercase, $uppercase)" />
                    <xsl:variable name="guess"
                      select="$ballots/player[@firstName = current()/@firstName and @lastName = current()/@lastName]/category[@name = $categoryName]/@nominee" />
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
                        <xsl:value-of
                          select="count($ballots/player/category[@name = $categoryName and @nominee = current()/@name])" />
                      </th>
                    </xsl:for-each>
                  </tr>
                </tfoot>
              </table>
            </xsl:otherwise>
          </xsl:choose>
        </center>
      </body>
      <xsl:call-template name="footer" />
    </html>
  </xsl:template>
  <xsl:template match="/definitions/column/nominee">
    <xsl:param name="category" />
    <td style="background-color: silver; text-align: center">
      <xsl:apply-templates select="." mode="poster">
        <xsl:with-param name="category" select="$category" />
      </xsl:apply-templates>
      <br />
      <xsl:value-of select="@name" />
    </td>
  </xsl:template>
  <xsl:template match="/definitions/column" mode="chart">
    <xsl:param name="border" />
    <img border="{$border}" alt="{@name}" title="{@name}">
      <xsl:attribute name="src">
        <xsl:value-of select="concat(@name, '.png?_=')" />
        <xsl:for-each
          select="$results/awards/category[@name = current()/@name]/nominee">
          <xsl:value-of select="concat(@name, '|')" />
        </xsl:for-each>
      </xsl:attribute>
    </img>
  </xsl:template>
</xsl:stylesheet>