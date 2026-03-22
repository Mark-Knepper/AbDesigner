<%-- 
    Document   : abdesigner_output
    Created on : Nov 13, 2010, 12:28:04 PM
    Author     : Pisitkun
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@include file="/includes/header.html"%>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <%
            String filename = (String) request.getAttribute("filename");
            String inputType = (String) request.getAttribute("inputType");
            String input = (String) request.getAttribute("input");
            String accession = (String) request.getAttribute("accession");
            Integer peptideLength = (Integer) request.getAttribute("peptideLength");
            Integer epitopeLength = (Integer) request.getAttribute("epitopeLength");
            String[][] arr2DIgScore = (String[][]) request.getAttribute("arr2DIgScore");
            String[][] arr2DUniqueness = (String[][]) request.getAttribute("arr2DUniqueness");
            String[][] arr2DConservation = (String[][]) request.getAttribute("arr2DConservation");
            Integer appletHeight = (Integer) request.getAttribute("appletHeight");
        %>
<title>AbDesigner: <%= input%></title>
<%@include file="/includes/title.html"%>
        <table cellspacing="5" cellpadding="5" border="0">
            <tr>
                <% if (!inputType.equals("FASTA")) {%>
                <td>
                    Input: <%= input%>
                </td>
                <% } else {%>
                <td>
                    Input: <input type="text" name="input" value="<%= input%>">
                </td>
                <% }%>
                <% if (accession != null) {%>
                <td>
                    UniProt: <a href="https://www.ncbi.nlm.nih.gov/protein/<%= accession%>" target="_blank" rel="noopener noreferrer"><%= accession%></a>
                </td>
                <% }%>
                <td>
                    Peptide Length: <%= peptideLength%>
                </td>
                <td>
                    Epitope Length: <%= epitopeLength%>
                </td>
            </tr>
        </table>
        <div id="heatmap-toolbar" style="padding:5px 10px; text-align:left;">
            <button id="exportImage" type="button">Export Image</button>
            <button id="exportRawData" type="button">Export Raw Data</button>
            <span style="margin-left:20px; font-size:small;">
                Download data: <a href="<%= "heatMapData/" + filename%>"><%= filename%></a>
                &nbsp;|&nbsp; Standalone viewer: <a href="HeatMap_viewer.jar">HeatMap_viewer.jar</a>
            </span>
        </div>
        <div id="heatmap-container" style="overflow-x:auto; border:1px solid #ccc; margin:0 10px; position:relative;">
            <canvas id="heatmap-canvas"></canvas>
            <div id="heatmap-tooltip" style="display:none; position:absolute; background:#c8d8f0; border:1px solid #000; padding:1px 3px; font-family:monospace; font-size:12px; color:#000; text-align:left; white-space:pre; z-index:1000; pointer-events:none;"></div>
        </div>
        <div id="heatmap-context-menu" style="display:none; position:fixed; background:#fff; border:1px solid #999; box-shadow:2px 2px 6px rgba(0,0,0,0.2); z-index:1001; font-size:13px; color:#000; text-align:left; cursor:default;">
            <div class="ctx-item" id="ctx-copy" style="padding:4px 16px;">Copy</div>
            <div class="ctx-item" id="ctx-nterm" style="padding:4px 16px;">Add N-terminal Cysteine + Copy</div>
            <div class="ctx-item" id="ctx-cterm" style="padding:4px 16px;">Add C-terminal Cysteine + Copy</div>
        </div>
        <style>
            .ctx-item:hover { background: #0078d4; color: #fff; }
        </style>
        <script>var heatMapData = <%= request.getAttribute("heatMapJson") %>;</script>
        <script src="js/heatmap.js"></script>
        <p></p>
        <table cellspacing="5" cellpadding="5" border="0" style="table-layout:auto; width:100%; max-width:1170px;">
            <col width=370>
            <col width=30>
            <col width=370>
            <col width=30>
            <col width=370>
            <tr>
                <td>
                    <label for="Ig-score_rank"><strong>Ig-score rank:</strong></label>
                </td>
                <td></td>
                <td>
                    <label for="Uniqueness-optimized_rank"><strong>Uniqueness-optimized rank:</strong></label>
                </td>
                <td></td>
                <td>
                    <label for="Conservation-optimized_rank"><strong>Conservation-optimized rank:</strong></label>
                </td>
            </tr>
            <tr>
                <td>
                    <textarea name="arr2DIgScore" id="Ig-score_rank" rows="10" cols="40" wrap="off" style="font-family: 'courier new', courier, monospace; font-size: small"><% for (int i = 0; i < arr2DIgScore.length; i++) {%><% if (Integer.parseInt(arr2DIgScore[i][0]) < 10) {%>
    <%= arr2DIgScore[i][0]%>: <%= arr2DIgScore[i][1]%> [<%= arr2DIgScore[i][2]%>-<%= arr2DIgScore[i][3]%>]<% } else if (Integer.parseInt(arr2DIgScore[i][0]) < 100) {%>
   <%= arr2DIgScore[i][0]%>: <%= arr2DIgScore[i][1]%> [<%= arr2DIgScore[i][2]%>-<%= arr2DIgScore[i][3]%>]<% } else if (Integer.parseInt(arr2DIgScore[i][0]) < 1000) {%>
  <%= arr2DIgScore[i][0]%>: <%= arr2DIgScore[i][1]%> [<%= arr2DIgScore[i][2]%>-<%= arr2DIgScore[i][3]%>]<% } else if (Integer.parseInt(arr2DIgScore[i][0]) < 10000) {%>
 <%= arr2DIgScore[i][0]%>: <%= arr2DIgScore[i][1]%> [<%= arr2DIgScore[i][2]%>-<%= arr2DIgScore[i][3]%>]<% } else {%>
<%= arr2DIgScore[i][0]%>: <%= arr2DIgScore[i][1]%> [<%= arr2DIgScore[i][2]%>-<%= arr2DIgScore[i][3]%>]<% }%><% }%>
                    </textarea>
                </td>
                <td></td>
                <td>
                    <textarea name="arr2DUniqueness" id="Uniqueness-optimized_rank" rows="10" cols="40" wrap="off" style="font-family: 'courier new', courier, monospace; font-size: small"><% for (int i = 0; i < arr2DUniqueness.length; i++) {%><% if (Integer.parseInt(arr2DUniqueness[i][0]) < 10) {%>
    <%= arr2DUniqueness[i][0]%>: <%= arr2DUniqueness[i][1]%> [<%= arr2DUniqueness[i][2]%>-<%= arr2DUniqueness[i][3]%>]<% } else if (Integer.parseInt(arr2DUniqueness[i][0]) < 100) {%>
   <%= arr2DUniqueness[i][0]%>: <%= arr2DUniqueness[i][1]%> [<%= arr2DUniqueness[i][2]%>-<%= arr2DUniqueness[i][3]%>]<% } else if (Integer.parseInt(arr2DUniqueness[i][0]) < 1000) {%>
  <%= arr2DUniqueness[i][0]%>: <%= arr2DUniqueness[i][1]%> [<%= arr2DUniqueness[i][2]%>-<%= arr2DUniqueness[i][3]%>]<% } else if (Integer.parseInt(arr2DUniqueness[i][0]) < 10000) {%>
 <%= arr2DUniqueness[i][0]%>: <%= arr2DUniqueness[i][1]%> [<%= arr2DUniqueness[i][2]%>-<%= arr2DUniqueness[i][3]%>]<% } else {%>
<%= arr2DUniqueness[i][0]%>: <%= arr2DUniqueness[i][1]%> [<%= arr2DUniqueness[i][2]%>-<%= arr2DUniqueness[i][3]%>]<% }%><% }%>
                    </textarea>
                </td>
                <td></td>
                <td>
                    <textarea name="arr2DConservation" id="Conservation-optimized_rank" rows="10" cols="40" wrap="off" style="font-family: 'courier new', courier, monospace; font-size: small"><% for (int i = 0; i < arr2DConservation.length; i++) {%><% if (Integer.parseInt(arr2DConservation[i][0]) < 10) {%>
    <%= arr2DConservation[i][0]%>: <%= arr2DConservation[i][1]%> [<%= arr2DConservation[i][2]%>-<%= arr2DConservation[i][3]%>]<% } else if (Integer.parseInt(arr2DConservation[i][0]) < 100) {%>
   <%= arr2DConservation[i][0]%>: <%= arr2DConservation[i][1]%> [<%= arr2DConservation[i][2]%>-<%= arr2DConservation[i][3]%>]<% } else if (Integer.parseInt(arr2DConservation[i][0]) < 1000) {%>
  <%= arr2DConservation[i][0]%>: <%= arr2DConservation[i][1]%> [<%= arr2DConservation[i][2]%>-<%= arr2DConservation[i][3]%>]<% } else if (Integer.parseInt(arr2DConservation[i][0]) < 10000) {%>
 <%= arr2DConservation[i][0]%>: <%= arr2DConservation[i][1]%> [<%= arr2DConservation[i][2]%>-<%= arr2DConservation[i][3]%>]<% } else {%>
<%= arr2DConservation[i][0]%>: <%= arr2DConservation[i][1]%> [<%= arr2DConservation[i][2]%>-<%= arr2DConservation[i][3]%>]<% }%><% }%>
                    </textarea>
                </td>
            </tr>
            <tr>
                <td valign="top">
                    "Ig-score rank" shows peptides from most immunogenic to least.
                </td>
                <td></td>
                <td valign="top">
                    "Uniqueness-optimized rank" shows Ig-score ranks for peptides predicted to be specific for target protein.
                </td>
                <td></td>
                <td valign="top">
                    "Conservation-optimized rank" shows Ig-score ranks for peptides predicted to be recognized in target protein in multiple species.
                </td>
            </tr>
        </table>
        <hr>
        <p><strong>Note:</strong> User is advised to add a cysteine (C) to one end of the selected peptide for conjugation to carrier and construction of affinity-purification column.  If a peptide containing an internal cysteine is chosen, an alternative cross-linking method should be chosen.</p>
        <%@include file="/includes/footer.html"%>
