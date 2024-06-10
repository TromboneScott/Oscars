<?xml version="1.0" encoding="utf-8"?>
<!-- OSCARS website created by Scott McDonald -->
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
  <xsl:include href="include.xsl" />
  <xsl:template match="/category">
    <html>
      <xsl:call-template name="init" />
      <xsl:variable name="categoryName" select="@name" />
      <xsl:variable name="categoryDefinition"
        select="$definitions/category[@name = $categoryName]" />
      <xsl:variable name="categoryResults"
        select="$results/categories/category[@name = $categoryName]" />
      <body>
        <center>
          <xsl:call-template name="header" />
          <div
            id="name">
            <xsl:value-of select="$categoryName" />
          </div> Tie Breaker: <xsl:value-of
            select="$categoryDefinition/@tieBreaker" />
          <xsl:if
            test="not(normalize-space($categoryDefinition/@tieBreaker))">
            NO
          </xsl:if>
          <br /> Point Value: <xsl:value-of
            select="@value" />
          <xsl:if
            test="count($categoryResults/winners/nominee) &gt; 1">
            <br />
            <br />
            <b>TIE</b> - Everyone who selected one of the winners in
          this category gets the points. </xsl:if>
          <br />
          <br />
          <xsl:apply-templates
            select="$categoryResults" mode="chart" />
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
                    <xsl:apply-templates select="$categoryResults/winners"
                      mode="attribute">
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
              <xsl:for-each select="player">
                <xsl:sort select="@lastName" />
                <xsl:sort select="@firstName" />
                <xsl:variable name="guess" select="@guess" />
                <tr>
                  <xsl:apply-templates select="$categoryResults/winners"
                    mode="attribute">
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
                <xsl:variable name="categoryData" select="." />
                <xsl:for-each select="$categoryDefinition/nominee">
                  <th>
                    <xsl:apply-templates select="$categoryResults/winners"
                      mode="attribute">
                      <xsl:with-param name="nominee" select="@name" />
                    </xsl:apply-templates>
                    <xsl:variable name="name" select="@name" />
                    <xsl:value-of
                      select="count($categoryData/player[@guess=$name])" />
                  </th>
                </xsl:for-each>
              </tr>
            </tfoot>
          </table>
          <br />
          <a
            href="all.xml" id="return">All Categories</a>
          <br />
          <br />
          <a
            href=".." id="return">Return to Main Page</a>
          <br />
          <br />
          <xsl:call-template
            name="updated" />
        </center>
      </body>
    </html>
  </xsl:template>
</xsl:stylesheet>