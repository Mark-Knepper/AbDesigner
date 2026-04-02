/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.nih.nhlbi.abdesigner;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.apache.commons.codec.digest.DigestUtils;

/**
 *
 * @author Pisitkun
 */
public class AbDesignerServlet extends HttpServlet {

    private HashMap<String, String> accessionLocusMap;
    private HashMap<String, String> locusAccessionMap;
    private HashMap<String, String> geneLocusMap;
    private String HEATMAP_DATA_PATH;
    private String DATABASE_PATH;
    private LinkedHashMap<String, Integer[]> locusFeaturesPositionMap;
    private LinkedHashMap<String, Integer[]> locusSequencePositionMap;

    @Override
    public void init() throws ServletException {
        super.init();
        HEATMAP_DATA_PATH = getServletContext().getRealPath("/heatMapData");
        DATABASE_PATH = getServletContext().getRealPath("/WEB-INF/database");
        try {
            readDatabase();
        } catch (IOException ex) {
            Logger.getLogger(AbDesignerServlet.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(AbDesignerServlet.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code> methods.
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    private static final String[] VALID_INPUT_TYPES = {"Gene Symbol", "Accession Number", "Entry Name", "FASTA"};
    private static final String[] VALID_SPECIES = {"HUMAN", "RAT", "MOUSE", "DROME", "CAEEL", "YEAST", "ARATH"};

    private static boolean isValidOption(String value, String[] validOptions) {
        if (value == null) return false;
        for (String option : validOptions) {
            if (option.equals(value)) return true;
        }
        return false;
    }

    private static String sanitizeInput(String input) {
        if (input == null) return "";
        return input.replaceAll("[<=>?&$%@\"+'`;\\\\|{}\\[\\]()!]", "").trim();
    }

    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException, ClassNotFoundException {

        String action = request.getParameter("action");
        if (action == null) action = "submit";

        switch (action) {
            case "submit":
                handleSubmit(request, response);
                break;
            case "status":
                handleStatus(request, response);
                break;
            case "result":
                handleResult(request, response);
                break;
            default:
                response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Unknown action");
                break;
        }
    }

    private void handleSubmit(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException, ClassNotFoundException {

        // get and validate parameters from the form
        String inputType = request.getParameter("inputType");
        String species = request.getParameter("species");
        String rawInput = request.getParameter("input");
        String peptideLengthStr = request.getParameter("peptideLength");
        String epitopeLengthStr = request.getParameter("epitopeLength");

        // validate required parameters
        if (!isValidOption(inputType, VALID_INPUT_TYPES)
                || !isValidOption(species, VALID_SPECIES)
                || rawInput == null || rawInput.trim().isEmpty()
                || peptideLengthStr == null || epitopeLengthStr == null) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid or missing parameters");
            return;
        }

        // validate numeric ranges
        int pepLen, epiLen;
        try {
            pepLen = Integer.parseInt(peptideLengthStr.replaceAll("\\D", ""));
            epiLen = Integer.parseInt(epitopeLengthStr.replaceAll("\\D", ""));
        } catch (NumberFormatException e) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid length parameters");
            return;
        }
        if (pepLen < 5 || pepLen > 50 || epiLen < 5 || epiLen > pepLen) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Length parameters out of range");
            return;
        }

        // sanitize input (server-side, mirrors client-side filtering)
        // FASTA input contains > | = [] characters needed for header parsing;
        // sequence lines are cleaned by the FASTA parser itself (line 195)
        String sanitizedInput = "FASTA".equals(inputType) ? rawInput.trim() : sanitizeInput(rawInput);
        sanitizedInput = sanitizedInput.toUpperCase();
        if (sanitizedInput.isEmpty()) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Input is empty after sanitization");
            return;
        }

        String input = sanitizedInput;
        int spanSetting = pepLen;
        int matchSpan = epiLen;

        // create objects for validation
        String[] inputParameters = {inputType, species, input, peptideLengthStr, epitopeLengthStr};
        String message = null;
        boolean isValidInput = false;
        boolean isAmbiguousGeneSymbol = false;

        // create objects for AbDesigner
        String entryName = "";
        String inputSequence = null;
        int inputSequenceLength;
        int numberOfSpan;

        // validate that input is in database
        if (inputType.equals("Gene Symbol")) {
            entryName = geneLocusMap.get(input + "_" + species);
            if (entryName != null) {
                if (!entryName.contains(",")) {
                    isValidInput = true;
                } else {
                    isAmbiguousGeneSymbol = true;
                }
            }
        } else if (inputType.equals("Accession Number")) {
            isValidInput = accessionLocusMap.containsKey(input.split("\\.")[0]);
            if (isValidInput) {
                entryName = accessionLocusMap.get(input.split("\\.")[0]);
            }
        } else if (inputType.equals("Entry Name")) {
            isValidInput = locusAccessionMap.containsKey(input);
            if (isValidInput) {
                entryName = input;
            }
        } else if (inputType.equals("FASTA")) {
            isValidInput = true;
            String[] strings = input.split("\\n"); // split by a newline
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < strings.length; i++) {
                if (!strings[i].startsWith(">")) {
                    sb.append(strings[i]);
                } else {
                    String[] description = strings[i].split("[\\|\\s]");
                    // description example >gi|728874|sp|P41181.1|AQP2_HUMAN
                    if (description.length >= 5 && description[2].equals("SP") && description[4].split("_").length == 2) {
                        entryName = description[4];
                    }
                }
            }
            // replaceAll digits, whitespace characters, and non-word characters
            inputSequence = sb.toString().replaceAll("[\\d\\s\\W]", "");
        }

        // validate that input is not too short or too long
        if (isValidInput) {
            inputSequence = (!inputType.equals("FASTA")) ? getValue(DATABASE_PATH + "/segmasker.ran", locusSequencePositionMap, entryName) : inputSequence;
            inputSequenceLength = inputSequence.length();
            if (inputSequenceLength < spanSetting) {
                isValidInput = false;
                message = "Protein too short: " + inputSequenceLength + " amino acids";
            } else if (inputSequenceLength > 10000) {
                isValidInput = false;
                message = "Protein too long: " + inputSequenceLength + " amino acids";
            }
        } else {
            inputSequenceLength = 0;
            if (isAmbiguousGeneSymbol) {
                message = input + " matches multiple Swiss-Prot Entry Names\n\n"
                        + "i.e. " + entryName + "\n\n"
                        + "Please select and enter one of the Swiss-Prot Entry Names";
                inputParameters[0] = "Entry Name";
            } else {
                message = "Invalid input";
            }
        }

        // validate the parameters
        if (!isValidInput) {
            String url = "/index.jsp";
            request.setAttribute("inputParameters", inputParameters);
            request.setAttribute("message", message);
            RequestDispatcher dispatcher =
                    getServletContext().getRequestDispatcher(url);
            dispatcher.forward(request, response);
        } else {
            numberOfSpan = inputSequenceLength - spanSetting + 1;
            String filename;
            String pathname;
            boolean fileExists;

            if (!inputType.equals("FASTA")) {
                filename = entryName + "_" + spanSetting + "_" + matchSpan + ".dat";
                pathname = HEATMAP_DATA_PATH + "/" + filename;
                fileExists = new File(pathname).exists();
            } else {
                filename = DigestUtils.shaHex(entryName + inputSequence) + "_" + spanSetting + "_" + matchSpan + ".dat";
                pathname = HEATMAP_DATA_PATH + "/" + filename;
                fileExists = new File(pathname).exists();
                if (fileExists) {
                    // verify FASTA cache is not a hash collision
                    HeatMapArguments cached = HeatMapDataIO.getHeatMapArguments(pathname);
                    if (!cached.getEntryName().equals(entryName) || !cached.getInputSequence().equals(inputSequence)) {
                        fileExists = false;
                    }
                }
            }

            if (fileExists) {
                // cache hit — go straight to results
                HttpSession session = request.getSession();
                session.setAttribute("asyncPathname", pathname);
                session.setAttribute("asyncFilename", filename);
                session.setAttribute("asyncEntryName", entryName);
                session.setAttribute("asyncInputType", inputType);
                session.setAttribute("asyncInput", input);
                session.setAttribute("asyncInputSequence", inputSequence);
                session.setAttribute("asyncSpanSetting", spanSetting);
                session.setAttribute("asyncMatchSpan", matchSpan);
                response.sendRedirect("AbDesignerServlet?action=result");
            } else {
                // cache miss — load database inputs and launch background computation
                final String fInputType = inputType;
                final String fEntryName = entryName;
                final String fInputSequence = inputSequence;
                final int fSpanSetting = spanSetting;
                final int fInputSequenceLength = inputSequenceLength;
                final int fNumberOfSpan = numberOfSpan;
                final int fMatchSpan = matchSpan;
                final String fPathname = pathname;

                final String swissProtContent;
                final ArrayList<ArrayList<String>> uniquenessDatabase;
                final ArrayList<ArrayList<String>> conservationDatabase;

                if (!inputType.equals("FASTA")) {
                    swissProtContent = getValue(DATABASE_PATH + "/features.ran", locusFeaturesPositionMap, entryName);
                    uniquenessDatabase = Database.get2DArrayList(DATABASE_PATH + "/" + entryName.split("_")[1] + "_locusSequenceArrayList.ab");
                    conservationDatabase = getConservationDatabase(entryName.split("_")[0]);
                } else {
                    swissProtContent = (entryName.isEmpty() || !locusAccessionMap.containsKey(entryName)) ? "" : getValue(DATABASE_PATH + "/features.ran", locusFeaturesPositionMap, entryName);
                    uniquenessDatabase = (entryName.isEmpty()) ? new ArrayList<ArrayList<String>>() : Database.get2DArrayList(DATABASE_PATH + "/" + entryName.split("_")[1] + "_locusSequenceArrayList.ab");
                    conservationDatabase = (entryName.isEmpty()) ? new ArrayList<ArrayList<String>>() : getConservationDatabase(entryName.split("_")[0]);
                }

                String errorPathname = pathname + ".error";
                new File(errorPathname).delete(); // clean up stale error file

                HttpSession session = request.getSession();
                synchronized (session) {
                    session.setAttribute("asyncPathname", pathname);
                    session.setAttribute("asyncFilename", filename);
                    session.setAttribute("asyncEntryName", entryName);
                    session.setAttribute("asyncInputType", inputType);
                    session.setAttribute("asyncInput", input);
                    session.setAttribute("asyncInputSequence", inputSequence);
                    session.setAttribute("asyncSpanSetting", spanSetting);
                    session.setAttribute("asyncMatchSpan", matchSpan);

                    new Thread(() -> {
                        try {
                            HeatMapArguments hma = AbDesigner.getHeatMapArguments(
                                    fInputType, fEntryName, swissProtContent, fInputSequence,
                                    fSpanSetting, fInputSequenceLength, fNumberOfSpan, fMatchSpan,
                                    uniquenessDatabase, conservationDatabase);
                            String tempPathname = fPathname + ".tmp";
                            HeatMapDataIO.add(hma, tempPathname);
                            if (!new File(tempPathname).renameTo(new File(fPathname))) {
                                throw new IOException("Failed to rename temp file to " + fPathname);
                            }
                        } catch (Exception e) {
                            try (PrintWriter pw = new PrintWriter(new FileWriter(errorPathname))) {
                                pw.println(e.getMessage() != null ? e.getMessage() : e.getClass().getName());
                            } catch (IOException ignored) {
                            }
                            Logger.getLogger(AbDesignerServlet.class.getName())
                                    .log(Level.SEVERE, "Background computation failed for " + fEntryName, e);
                        }
                    }, "AbDesigner-compute-" + entryName).start();
                }

                // show the polling wait page
                String proteinName = entryName.isEmpty() ? "FASTA sequence" : entryName;
                request.setAttribute("proteinName", proteinName);
                request.setAttribute("sequenceLength", inputSequenceLength);
                RequestDispatcher dispatcher =
                        getServletContext().getRequestDispatcher("/abdesigner_wait.jsp");
                dispatcher.forward(request, response);
            }
        }
    }

    private void handleStatus(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        response.setContentType("application/json;charset=UTF-8");
        HttpSession session = request.getSession(false);
        PrintWriter out = response.getWriter();
        try {
            if (session == null || session.getAttribute("asyncPathname") == null) {
                out.print("{\"error\":\"No computation in progress\"}");
                return;
            }
            String pathname = (String) session.getAttribute("asyncPathname");
            if (new File(pathname).exists()) {
                out.print("{\"ready\":true}");
            } else if (new File(pathname + ".error").exists()) {
                String errMsg = new String(Files.readAllBytes(Paths.get(pathname + ".error"))).trim();
                errMsg = errMsg.replace("\\", "\\\\").replace("\"", "\\\"");
                out.print("{\"error\":\"" + errMsg + "\"}");
            } else {
                out.print("{\"ready\":false}");
            }
        } finally {
            out.close();
        }
    }

    private void handleResult(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException, ClassNotFoundException {
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("asyncPathname") == null) {
            response.sendRedirect("index.jsp");
            return;
        }

        String pathname = (String) session.getAttribute("asyncPathname");
        String filename = (String) session.getAttribute("asyncFilename");
        String entryName = (String) session.getAttribute("asyncEntryName");
        String inputType = (String) session.getAttribute("asyncInputType");
        String input = (String) session.getAttribute("asyncInput");
        String inputSequence = (String) session.getAttribute("asyncInputSequence");
        int spanSetting = (int) session.getAttribute("asyncSpanSetting");
        int matchSpan = (int) session.getAttribute("asyncMatchSpan");

        if (!new File(pathname).exists()) {
            response.sendRedirect("index.jsp");
            return;
        }

        HeatMapArguments heatMapArguments = HeatMapDataIO.getHeatMapArguments(pathname);
        String accession = locusAccessionMap.get(entryName);
        String[][] arr2DIgScore = heatMapArguments.getArr2DIgScore();
        String[][] arr2DUniqueness = heatMapArguments.getArr2DUniqueness();
        String[][] arr2DConservation = heatMapArguments.getArr2DConservation();
        int regionSiteNumber = heatMapArguments.getArrayAllRegion().length + heatMapArguments.getArrayAllSite().length;
        int appletHeight = (regionSiteNumber > 15) ? 628 : (328 + (regionSiteNumber * 20));

        String url = "/abdesigner_output.jsp";
        request.setAttribute("filename", filename);
        request.setAttribute("inputType", inputType);
        if (!inputType.equals("FASTA")) {
            request.setAttribute("input", entryName);
        } else {
            request.setAttribute("input", (entryName.isEmpty()) ? inputSequence : (entryName + ": " + inputSequence));
        }
        request.setAttribute("accession", accession);
        request.setAttribute("peptideLength", spanSetting);
        request.setAttribute("epitopeLength", matchSpan);
        request.setAttribute("arr2DIgScore", arr2DIgScore);
        request.setAttribute("arr2DUniqueness", arr2DUniqueness);
        request.setAttribute("arr2DConservation", arr2DConservation);
        request.setAttribute("appletHeight", appletHeight);
        request.setAttribute("heatMapJson", heatMapArguments.toJson());
        RequestDispatcher dispatcher =
                getServletContext().getRequestDispatcher(url);
        dispatcher.forward(request, response);
    }

    // <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
    /**
     * Handles the HTTP <code>GET</code> method.
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            processRequest(request, response);
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(AbDesignerServlet.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Handles the HTTP <code>POST</code> method.
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            processRequest(request, response);
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(AbDesignerServlet.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Returns a short description of the servlet.
     * @return a String containing servlet description
     */
    @Override
    public String getServletInfo() {
        return "Short description";
    }// </editor-fold>

    private void readDatabase() throws IOException, ClassNotFoundException {
        //Read from AbDesigner database [.ab]
        accessionLocusMap = Database.getHashMap(DATABASE_PATH + "/ARATH_CAEEL_DROME_HUMAN_MOUSE_RAT_YEAST_accessionLocusMap.ab");
        locusAccessionMap = Database.getHashMap(DATABASE_PATH + "/ARATH_CAEEL_DROME_HUMAN_MOUSE_RAT_YEAST_locusAccessionMap.ab");
        geneLocusMap = Database.getHashMap(DATABASE_PATH + "/ARATH_CAEEL_DROME_HUMAN_MOUSE_RAT_YEAST_geneLocusMap.ab");
        locusFeaturesPositionMap = Database.getLinkedHashMap(DATABASE_PATH + "/ARATH_CAEEL_DROME_HUMAN_MOUSE_RAT_YEAST_locusFeaturesPositionMap.ab");
        locusSequencePositionMap = Database.getLinkedHashMap(DATABASE_PATH + "/ARATH_CAEEL_DROME_HUMAN_MOUSE_RAT_YEAST_locusSequencePositionMap.ab");
    }

    private ArrayList<ArrayList<String>> getConservationDatabase(String gene) throws FileNotFoundException, IOException {
        ArrayList<ArrayList<String>> conservationDatabase = new ArrayList<ArrayList<String>>();
        String humanEntryName = gene + "_HUMAN";
        String ratEntryName = gene + "_RAT";
        String mouseEntryName = gene + "_MOUSE";
        String humanSequence = getValue(DATABASE_PATH + "/segmasker.ran", locusSequencePositionMap, humanEntryName);
        String ratSequence = getValue(DATABASE_PATH + "/segmasker.ran", locusSequencePositionMap, ratEntryName);
        String mouseSequence = getValue(DATABASE_PATH + "/segmasker.ran", locusSequencePositionMap, mouseEntryName);
        if (humanSequence != null) {
            ArrayList<String> arrayList = new ArrayList<String>();
            arrayList.add(humanEntryName);
            arrayList.add(humanSequence.toUpperCase());
            conservationDatabase.add(arrayList);
        }
        if (ratSequence != null) {
            ArrayList<String> arrayList = new ArrayList<String>();
            arrayList.add(ratEntryName);
            arrayList.add(ratSequence.toUpperCase());
            conservationDatabase.add(arrayList);
        }
        if (mouseSequence != null) {
            ArrayList<String> arrayList = new ArrayList<String>();
            arrayList.add(mouseEntryName);
            arrayList.add(mouseSequence.toUpperCase());
            conservationDatabase.add(arrayList);
        }
        return conservationDatabase;
    }

    private String getValue(String file, LinkedHashMap<String, Integer[]> linkedHashMap, String entryName) throws FileNotFoundException, IOException {
        String value = null;
        Integer[] integers = linkedHashMap.get(entryName);
        if (integers != null) {
            RandomAccessFile randomAccessFile = null;
            try {
                randomAccessFile = new RandomAccessFile(file, "r");
                randomAccessFile.seek(integers[0] * 2); //the offset position, two bytes per character
                value = readFixedString(randomAccessFile, integers[1]); //data length
            } finally {
                if (randomAccessFile != null) {
                    randomAccessFile.close();
                }
            }
        }
        return value;
    }

    private String readFixedString(DataInput in, Integer length) throws IOException {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < length; i++) {
            char c = in.readChar(); //Reads two input bytes and returns a char value
            sb.append(c);
        }
        return sb.toString();
    }
}
