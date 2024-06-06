<?xml version="1.0" encoding="utf-8"?>
<!-- OSCARS website created by Scott McDonald -->
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
  <xsl:include href="include.xsl" />
  <xsl:template match="/definitions">
    <html>
      <xsl:call-template name="init" />
      <body>
        <center>
          <xsl:call-template name="header" />
          <div id="name">
            Definitions
          </div>
          <br />
          <h2>
            Tie Breakers
          </h2>
          <table>
            <tbody>
              <tr>
                <th>
                  #
                </th>
                <th>
                  Category
                </th>
              </tr>
              <xsl:for-each select="category[@tieBreaker]">
                <xsl:sort select="@tieBreaker" />
                <tr>
                  <td class="unannounced">
                    <xsl:value-of select="@tieBreaker" />
                  </td>
                  <td class="unannounced">
                    <xsl:value-of select="@name" />
                  </td>
                </tr>
              </xsl:for-each>
            </tbody>
          </table>
          <br />
          <br />
          <hr />
          <h2>
            Categories
          </h2>
          <i> Values for this website are in <b>bold</b>
          </i>
          <xsl:for-each select="category">
            <br />
            <br />
            <hr align="center" width="50%" />
            <br />
            <b>
              <xsl:value-of select="@name" />
            </b>
            <br />
            <xsl:variable name="category" select="@name" />
            <xsl:value-of
              select="$mappings/category[@name=$category]/@ballot" />
            <xsl:if test="nominee">
              <table>
                <tbody>
                  <xsl:for-each select="nominee">
                    <tr>
                      <xsl:apply-templates
                        select="$results/categories/category[@name = $category]/winners"
                        mode="attribute">
                        <xsl:with-param name="nominee" select="@name" />
                      </xsl:apply-templates>
                      <td>
                        <xsl:call-template name="poster">
                          <xsl:with-param name="category" select="$category" />
                          <xsl:with-param name="nominee" select="@name" />
                        </xsl:call-template>
                      </td>
                      <td>
                        <b>
                          <xsl:value-of select="@name" />
                        </b>
                        <xsl:variable name="website" select="@name" />
                        <xsl:for-each
                          select="$mappings/category[@name=$category]/map[@website=$website]">
                          <br />
                          <xsl:value-of select="@ballot" />
                        </xsl:for-each>
                      </td>
                    </tr>
                  </xsl:for-each>
                </tbody>
              </table>
            </xsl:if>
          </xsl:for-each>
          <br />
          <br />
          <hr />
        </center>
      </body>
    </html>
  </xsl:template>
</xsl:stylesheet>