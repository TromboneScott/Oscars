<?xml version="1.0" encoding="utf-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
  <xsl:output method="html" encoding="utf-8" indent="yes" />
  <xsl:include href="header.xsl" />
  <xsl:template match="/category">
    <xsl:text disable-output-escaping='yes'>&lt;!DOCTYPE html&gt;</xsl:text>
    <html>
      <xsl:variable name="categoryName" select="@name" />
      <xsl:variable name="categoryDefinition"
        select="document('../categoryDefinitions.xml')/categories/category[@name = $categoryName]" />
      <xsl:variable name="categoryData"
        select="document('../category/all.xml')/categories/category[@name = $categoryName]" />
      <xsl:variable name="results" select="document('../results.xml')/results" />
      <xsl:variable name="categoryResults"
        select="$results/categories/category[@name = $categoryName]" />
      <xsl:call-template name="init">
        <xsl:with-param name="results" select="$results" />
      </xsl:call-template>
      <body>
        <center>
          <xsl:call-template name="header">
            <xsl:with-param name="results" select="$results" />
          </xsl:call-template>
          <div
            id="name">
            <xsl:value-of select="$categoryName" />
          </div> Tie Breaker: <xsl:value-of
            select="$categoryDefinition/@tieBreaker" />
          <xsl:if
            test="$categoryDefinition/@tieBreaker = ''">
            NO
          </xsl:if>
          <br /> Point Value: <xsl:value-of
            select="$categoryData/@value" />
          <xsl:if
            test="count($categoryResults/winners/nominee) &gt; 1">
            <br />
            <br />
            <b>TIE</b> - Everyone who selected one of the winners in
          this category gets the points. </xsl:if>
          <br />
          <br />
          <xsl:call-template
            name="chart">
            <xsl:with-param name="categoryResults" select="$categoryResults" />
          </xsl:call-template>
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
                    <xsl:call-template name="status">
                      <xsl:with-param name="nominee" select="@name" />
                      <xsl:with-param name="winners"
                        select="$categoryResults/winners" />
                    </xsl:call-template>
                    <xsl:call-template name="poster">
                      <xsl:with-param name="category" select="$categoryName" />
                      <xsl:with-param name="nominee" select="@name" />
                    </xsl:call-template>
                    <br />
                    <xsl:value-of select="." />
                  </th>
                </xsl:for-each>
              </tr>
            </thead>
            <tbody>
              <xsl:for-each select="$categoryData/player">
                <xsl:sort select="@lastName" />
                <xsl:sort select="@firstName" />
                <xsl:variable name="guess" select="@guess" />
                <xsl:variable name="playerDisplayName">
                  <xsl:value-of select="@lastName" />
                  <xsl:if test="@firstName != '' and @lastName != ''">
                    <xsl:value-of select="', '" />
                  </xsl:if>
                  <xsl:value-of select="@firstName" />
                </xsl:variable>
                <tr>
                  <xsl:call-template name="status">
                    <xsl:with-param name="nominee" select="$guess" />
                    <xsl:with-param name="winners"
                      select="$categoryResults/winners" />
                  </xsl:call-template>
                  <td class="header">
                    <a>
                      <xsl:attribute name="href">
                        <xsl:value-of select="concat('../player/', @webPage)" />
                      </xsl:attribute>
                      <xsl:value-of select="$playerDisplayName" />
                    </a>
                  </td>
                  <xsl:for-each select="$categoryDefinition/nominee">
                    <td id="selection">
                      <xsl:if test="@name = $guess">
                        &#10004;
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
                    <xsl:call-template name="status">
                      <xsl:with-param name="nominee" select="@name" />
                      <xsl:with-param name="winners"
                        select="$categoryResults/winners" />
                    </xsl:call-template>
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
            name="updated">
            <xsl:with-param name="results" select="$results" />
          </xsl:call-template>
        </center>
      </body>
    </html>
  </xsl:template>
</xsl:stylesheet>