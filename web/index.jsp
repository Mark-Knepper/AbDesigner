<%-- 
    Document   : index
    Created on : Nov 6, 2010, 9:57:00 PM
    Author     : Pisitkun
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@include file="/includes/header.html"%>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<%@include file="/includes/jQuery.html"%>
<title>AbDesigner</title>
<%@include file="/includes/title.html"%>
<noscript style="color: lime">
<strong>Attention Users:</strong> It appears that your browser does not support JavaScript!
AbDesigner requires your browser to be JavaScript enabled. 
Please enable JavaScript in your browser.
</noscript>
<script type="text/javascript">
    function validate(form) {
        if (form.input.value.trim() === "") {
            alert("Please enter input");
            form.input.focus();
        } else if (parseInt(form.epitopeLength.value, 10) > parseInt(form.peptideLength.value, 10)) {
            alert("Epitope Length cannot be longer than Peptide Length");
            form.epitopeLength.focus();
        } else {
            var isFasta = document.getElementById("FASTA").checked;
            if (!isFasta) {
                var rawValue = document.getElementById("input").value;
                var sanitizedValue = rawValue.replace(/[<\=\?&\$%@"\+]/g, "");
                document.getElementById("input").value = sanitizedValue;
            }
            form.submit();
        }
    }
    function clearInput(form) {
        form.input.value = "";
    }
</script>
<%
    // get attributes from the request
    String[] inputParameters = (String[]) request.getAttribute("inputParameters");
    String message = (String) request.getAttribute("message");

    String[] checkedArray = {"", "", "", ""};
    String[] selectedArray = {"", "", "", "", "", "", ""};
    String peptideLength;
    String epitopeLength;

    // handle null values
    if (inputParameters == null) {
        checkedArray[0] = "checked";
        selectedArray[0] = "selected";
        peptideLength = "15";
        epitopeLength = "7";
    } else {
        if (inputParameters[0].equals("Gene Symbol")) {
            checkedArray[0] = "checked";
        } else if (inputParameters[0].equals("Accession Number")) {
            checkedArray[1] = "checked";
        } else if (inputParameters[0].equals("Entry Name")) {
            checkedArray[2] = "checked";
        } else if (inputParameters[0].equals("FASTA")) {
            checkedArray[3] = "checked";
        }
        if (inputParameters[1].equals("HUMAN")) {
            selectedArray[0] = "selected";
        } else if (inputParameters[1].equals("RAT")) {
            selectedArray[1] = "selected";
        } else if (inputParameters[1].equals("MOUSE")) {
            selectedArray[2] = "selected";
        } else if (inputParameters[1].equals("DROME")) {
            selectedArray[3] = "selected";
        } else if (inputParameters[1].equals("CAEEL")) {
            selectedArray[4] = "selected";
        } else if (inputParameters[1].equals("YEAST")) {
            selectedArray[5] = "selected";
        } else if (inputParameters[1].equals("ARATH")) {
            selectedArray[6] = "selected";
        }
        peptideLength = inputParameters[3];
        epitopeLength = inputParameters[4];
    }
    String target = "";
    if (message == null) {
        message = "";
        target = "target='_blank'";
    }
%>
<form action="<%= response.encodeURL("AbDesignerServlet")%>" <%= target%> method="post">
    <table cellspacing="5" cellpadding="5" border="0">
        <tr>
            <td align="right">
                Select Input Type <img id="inputTypeDialogOpener" style="vertical-align:text-top" title="Click here for more details" src="images/question_blue.png" alt="question_blue" height="20">
            </td>
            <td>
                <fieldset style="border: none; padding: 0"><legend></legend>
                    <table>
                        <tr>
                            <td>
                                <input type="radio" name="inputType" id="Gene_Symbol" value="Gene Symbol" <%= checkedArray[0]%>><label for="Gene_Symbol">Gene Symbol</label>
                                <label for="species">
                                    <select name="species" id="species">
                                        <option value="HUMAN" <%= selectedArray[0]%>>Human</option>
                                        <option value="RAT" <%= selectedArray[1]%>>Rat</option>
                                        <option value="MOUSE" <%= selectedArray[2]%>>Mouse</option>
                                        <option value="DROME" <%= selectedArray[3]%>>D. melanogaster</option>
                                        <option value="CAEEL" <%= selectedArray[4]%>>C. elegans</option>
                                        <option value="YEAST" <%= selectedArray[5]%>>S. cerevisiae</option>
                                        <option value="ARATH" <%= selectedArray[6]%>>Arabidopsis</option>
                                    </select>
                                </label>
                            </td>
                            <td>
                                (e.g. AQP2)
                            </td>
                        </tr>
                        <tr>
                            <td>
                                <input type="radio" name="inputType" id="Accession_Number" value="Accession Number" <%= checkedArray[1]%>><label for="Accession_Number">Swiss-Prot Accession Number</label>
                            </td>
                            <td>
                                (e.g. P41181)
                            </td>
                        </tr>
                        <tr>
                            <td>
                                <input type="radio" name="inputType" id="Entry_Name" value="Entry Name" <%= checkedArray[2]%>><label for="Entry_Name">Swiss-Prot Entry Name</label>
                            </td>
                            <td>
                                (e.g. AQP2_HUMAN)
                            </td>
                        </tr>
                        <tr>
                            <td>
                                <input type="radio" name="inputType" id="FASTA" value="FASTA" <%= checkedArray[3]%>><label for="FASTA">FASTA Amino Acid Sequence</label>
                            </td>
                        </tr>
                    </table>
                </fieldset>
            </td>
            <td>
                <div id="inputTypeDialog" title="Input Type" style="text-align: left; font-size: smaller">
                    To specify a protein for analysis, the program accepts the following types of input:
                    <ul>
                        <li>Official Gene Symbol</li>
                        <li>Swiss-Prot Accession Number</li>
                        <li>Swiss-Prot Entry Name</li>
                    </ul>
                    from any of the following seven species:
                    <ul>
                        <li>Homo sapiens</li>
                        <li>Rattus norvegicus</li>
                        <li>Mus musculus</li>
                        <li>Drosophila melanogaster</li>
                        <li>Caenorhabditis elegans</li>
                        <li>Saccharomyces cerevisiae</li>
                        <li>Arabidopsis thaliana</li>
                    </ul>
                    Proteins from other species can be analyzed by entering the sequence of that protein FASTA format
                    (This should be avoided when analyzing proteins from the above seven species because of the limitations associated with submitting a FASTA amino acid sequence as described in the <a style="color: blue" href="faq.html#limitations" target="_blank" rel="noopener noreferrer">Frequently Asked Questions</a>).
                </div>
            </td>
        </tr>
        <tr>
            <td align="right">
                <label for="input">Enter Input <img id="inputDialogOpener" style="vertical-align:text-top" title="Click here for more details" src="images/question_blue.png" alt="question_blue" height="20"></label>
            </td>
            <td>
                <textarea name="input" id="input" rows="6" cols="60" wrap="off" onfocus="autocompleteUI()"><%= message%></textarea>
            </td>
            <td>
                <div id="inputDialog" title="Input" style="text-align: left; font-size: smaller">
                    <ul>
                        <li>Autocomplete widget provides suggestions while you type into the field. <span style="color: red">(Does not work with FASTA input)</span></li>
                        <li>The suggestions are displayed when at least two characters are entered into the field.</li>
                    </ul>
                </div>
            </td>
        </tr>
        <tr>
            <td align="right">
                <label for="peptideLength">Peptide Length <img id="peptideLengthDialogOpener" style="vertical-align:text-top" title="Click here for more details" src="images/question_blue.png" alt="question_blue" height="20"></label>
            </td>
            <td>
                <select name="peptideLength" id="peptideLength">
                    <option value="5">5</option>
                    <option value="6">6</option>
                    <option value="7">7</option>
                    <option value="8">8</option>
                    <option value="9">9</option>
                    <option value="10">10</option>
                    <option value="11">11</option>
                    <option value="12">12</option>
                    <option value="13">13</option>
                    <option value="14">14</option>
                    <option value="15" selected>15</option>
                    <option value="16">16</option>
                    <option value="17">17</option>
                    <option value="18">18</option>
                    <option value="19">19</option>
                    <option value="20">20</option>
                    <option value="21">21</option>
                    <option value="22">22</option>
                    <option value="23">23</option>
                    <option value="24">24</option>
                    <option value="25">25</option>
                    <option value="26">26</option>
                    <option value="27">27</option>
                    <option value="28">28</option>
                    <option value="29">29</option>
                    <option value="30">30</option>
                    <option value="31">31</option>
                    <option value="32">32</option>
                    <option value="33">33</option>
                    <option value="34">34</option>
                    <option value="35">35</option>
                    <option value="36">36</option>
                    <option value="37">37</option>
                    <option value="38">38</option>
                    <option value="39">39</option>
                    <option value="40">40</option>
                    <option value="41">41</option>
                    <option value="42">42</option>
                    <option value="43">43</option>
                    <option value="44">44</option>
                    <option value="45">45</option>
                    <option value="46">46</option>
                    <option value="47">47</option>
                    <option value="48">48</option>
                    <option value="49">49</option>
                    <option value="50">50</option>
                </select>
                (5-50 amino acids)
            </td>
            <td>
                <div id="peptideLengthDialog" title="Peptide Length" style="text-align: left; font-size: smaller">
                    <ul>
                        <li>A user-defined sequence length for production of synthetic peptides for immunization can be set from 5 to 50 amino acids.</li>
                        <li>A peptide of 10-25 amino acids is typically used.</li>
                        <li>A longer sequence can provide a greater likelihood of producing a potent antibody by virtue of the fact that it contains more potential epitopes, but by the same token there is a greater chance that it will produce IgG clones with lower specificity.</li>
                        <li>Furthermore, the cost of peptide synthesis often increases substantially with larger peptides.</li>
                    </ul>
                </div>
            </td>
        </tr>
        <tr>
            <td align="right">
                <label for="epitopeLength">Epitope Length <img id="epitopeLengthDialogOpener" style="vertical-align:text-top" title="Click here for more details" src="images/question_blue.png" alt="question_blue" height="20"></label>
            </td>
            <td>
                <select name="epitopeLength" id="epitopeLength">
                    <option value="5">5</option>
                    <option value="6">6</option>
                    <option value="7" selected>7</option>
                    <option value="8">8</option>
                    <option value="9">9</option>
                    <option value="10">10</option>
                    <option value="11">11</option>
                    <option value="12">12</option>
                    <option value="13">13</option>
                    <option value="14">14</option>
                    <option value="15">15</option>
                    <option value="16">16</option>
                    <option value="17">17</option>
                    <option value="18">18</option>
                    <option value="19">19</option>
                    <option value="20">20</option>
                    <option value="21">21</option>
                    <option value="22">22</option>
                    <option value="23">23</option>
                    <option value="24">24</option>
                    <option value="25">25</option>
                    <option value="26">26</option>
                    <option value="27">27</option>
                    <option value="28">28</option>
                    <option value="29">29</option>
                    <option value="30">30</option>
                    <option value="31">31</option>
                    <option value="32">32</option>
                    <option value="33">33</option>
                    <option value="34">34</option>
                    <option value="35">35</option>
                    <option value="36">36</option>
                    <option value="37">37</option>
                    <option value="38">38</option>
                    <option value="39">39</option>
                    <option value="40">40</option>
                    <option value="41">41</option>
                    <option value="42">42</option>
                    <option value="43">43</option>
                    <option value="44">44</option>
                    <option value="45">45</option>
                    <option value="46">46</option>
                    <option value="47">47</option>
                    <option value="48">48</option>
                    <option value="49">49</option>
                    <option value="50">50</option>
                </select>
                (&le; Peptide Length, minimum 5 amino acids)
            </td>
            <td>
                <div id="epitopeLengthDialog" title="Epitope Length" style="text-align: left; font-size: smaller">
                    <ul>
                        <li>A user-defined linear &#39;Epitope Length&#39; can be set from 5 amino acids to the full length of the peptide.</li>
                        <li>Only the 5-10 amino acids in each hypervariable region form the antigen-binding site of an antibody molecule. As a result, the size of the antigenic determinant (epitope) that an antibody recognizes is generally comparably small.</li>
                        <li>The &#39;Epitope Length&#39; is used for determination of uniqueness and conservation of a peptide as described in the <a style="color: blue" href="faq.html#epitopeLength" target="_blank" rel="noopener noreferrer">Frequently Asked Questions</a>.</li>
                    </ul>
                </div>
            </td>
        </tr>
        <tr>
            <td>

            </td>
            <td>
                <input type="button" value="Submit" onclick="validate(this.form)">
                <input type="button" value="Clear input" onclick="clearInput(this.form)">
            </td>
        </tr>
    </table>
</form>
<%@include file="/includes/footer.html"%>
