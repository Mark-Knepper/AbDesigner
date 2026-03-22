(function () {
    "use strict";

    var d = heatMapData;
    var seqLen = d.inputSequenceLength;
    var span = d.spanSetting;
    var left = Math.floor((span - 1) / 2);
    var right = Math.floor((span + 2) / 2);
    var RECT_W = 7;
    var LABEL_W = 120;
    var BG = "#eeeeee";

    // Layout Y positions and heights
    var layout = {
        topFiller: { y: 0, h: 40 },
        position: { y: 40, h: 20 },
        residue: { y: 60, h: 20 },
        structure: { y: 80, h: 20 },
        hydropathy: { y: 104, h: 15 },
        preFiller: { y: 120, h: 4 },
        igScore: { y: 124, h: 40 },
        uniqueness: { y: 164, h: 40 },
        conservation: { y: 204, h: 40 },
        midFiller: { y: 244, h: 4 },
        regionSiteStart: 248
    };

    // Build active region/site rows
    var regionSiteRows = [];
    for (var i = 0; i < d.arrayAllRegion.length; i++) {
        if (d.arrayAllRegionSelectedState[i]) {
            regionSiteRows.push({ label: d.arrayAllRegion[i], index: i, type: "region" });
        }
    }
    for (var i = 0; i < d.arrayAllSite.length; i++) {
        if (d.arrayAllSiteSelectedState[i]) {
            regionSiteRows.push({ label: d.arrayAllSite[i], index: i + d.arrayAllRegion.length, type: "site" });
        }
    }

    var RIGHT_FILLER = 120;
    var totalHeight = layout.regionSiteStart + regionSiteRows.length * 20 + 20;
    var totalWidth = LABEL_W + seqLen * RECT_W + RIGHT_FILLER;

    var canvas = document.getElementById("heatmap-canvas");
    var ctx = canvas.getContext("2d");
    canvas.width = totalWidth;
    canvas.height = totalHeight;
    canvas.style.cursor = "crosshair";

    var tooltip = document.getElementById("heatmap-tooltip");
    var ctxMenu = document.getElementById("heatmap-context-menu");

    // State
    var frozen = false;
    var frozenIdx = -1;

    // --- Color helpers ---
    function interpColor(intensity, maxR, maxG, maxB, minR, minG, minB) {
        var r = Math.floor((intensity * (maxR - minR)) / 255) + minR;
        var g = Math.floor((intensity * (maxG - minG)) / 255) + minG;
        var b = Math.floor((intensity * (maxB - minB)) / 255) + minB;
        return "rgb(" + clamp(r) + "," + clamp(g) + "," + clamp(b) + ")";
    }

    function clamp(v) { return Math.max(0, Math.min(255, v)); }

    function igScoreColor(intensity) { return interpColor(intensity, 0, 255, 0, 0, 0, 0); }
    function uniquenessColor(intensity) { return interpColor(intensity, 255, 255, 0, 0, 0, 0); }
    function conservationColor(intensity) { return interpColor(intensity, 255, 0, 0, 0, 0, 0); }
    function kdColor(intensity) { return interpColor(intensity, 0, 0, 0, 0, 255, 255); }

    function structureColor(name) {
        switch (name) {
            case "alpha helix": return "#666666";
            case "beta sheet": return "#000000";
            case "strong beta turn": return "#ff0000";
            case "weak beta turn": return "#ff9999";
            default: return null;
        }
    }

    // Java String.hashCode() for region/site colors
    function javaHashCode(s) {
        var h = 0;
        for (var i = 0; i < s.length; i++) {
            h = (Math.imul(31, h) + s.charCodeAt(i)) | 0;
        }
        return h;
    }

    function hashColor(s) {
        var h = javaHashCode(s);
        var r = (h >> 16) & 0xff;
        var g = (h >> 8) & 0xff;
        var b = h & 0xff;
        return "rgb(" + r + "," + g + "," + b + ")";
    }

    // --- Drawing ---
    function draw(highlightIdx, highlightColor) {
        ctx.fillStyle = BG;
        ctx.fillRect(0, 0, canvas.width, canvas.height);

        drawLabels();
        drawPositionNumbers();
        drawResidues(highlightIdx, highlightColor);
        drawStructure();
        drawHydropathy();
        drawSeparator(layout.preFiller.y, layout.preFiller.h);
        drawHeatRow(d.arrIgScoreHeatMap, layout.igScore.y, layout.igScore.h, igScoreColor);
        drawHeatRow(d.arrUniquenessHeatMap, layout.uniqueness.y, layout.uniqueness.h, uniquenessColor);
        drawHeatRow(d.arrConservationHeatMap, layout.conservation.y, layout.conservation.h, conservationColor);
        drawSeparator(layout.midFiller.y, layout.midFiller.h);
        drawRegionSiteRows(highlightIdx, highlightColor);
    }

    function drawLabels() {
        ctx.fillStyle = BG;
        ctx.fillRect(0, 0, LABEL_W, canvas.height);

        ctx.fillStyle = "#000";
        ctx.font = "12px sans-serif";
        ctx.textAlign = "center";
        ctx.textBaseline = "middle";

        ctx.fillText("Chou-Fasman", LABEL_W / 2, layout.structure.y + layout.structure.h / 2);
        ctx.fillText("K-D Hydropathy", LABEL_W / 2, layout.hydropathy.y + layout.hydropathy.h / 2);
        ctx.fillText("Ig-score", LABEL_W / 2, layout.igScore.y + layout.igScore.h / 2);
        ctx.fillText("Uniqueness", LABEL_W / 2, layout.uniqueness.y + layout.uniqueness.h / 2);
        ctx.fillText("Conservation", LABEL_W / 2, layout.conservation.y + layout.conservation.h / 2);

        for (var i = 0; i < regionSiteRows.length; i++) {
            var y = layout.regionSiteStart + i * 20;
            var label = regionSiteRows[i].label;
            // Truncate long labels
            ctx.font = "11px sans-serif";
            var text = label.length > 18 ? label.substring(0, 16) + ".." : label;
            ctx.fillText(text, LABEL_W / 2, y + 10);
        }
    }

    function drawPositionNumbers() {
        ctx.fillStyle = "#000";
        ctx.font = "12px monospace";
        ctx.textAlign = "left";
        ctx.textBaseline = "top";

        for (var i = 0; i < seqLen; i++) {
            if (i % 10 === 0) {
                var x = LABEL_W + i * RECT_W;
                var numStr = String(i + 1);
                // Center the number over the tick
                var offset = 0;
                if (i >= 10 && i < 100) offset = -4;
                else if (i >= 100 && i < 1000) offset = -7;
                else if (i >= 1000) offset = -11;
                ctx.fillText(numStr, x + offset, layout.position.y);
                // Tick mark
                ctx.beginPath();
                ctx.moveTo(x + 3, layout.position.y + 18);
                ctx.lineTo(x + 3, layout.position.y + 20);
                ctx.stroke();
            }
        }
    }

    function drawResidues(highlightIdx, highlightColor) {
        var y = layout.residue.y;
        ctx.font = "12px monospace";
        ctx.textBaseline = "top";

        for (var i = 0; i < seqLen; i++) {
            var x = LABEL_W + i * RECT_W;
            var ch = d.segmaskerChar.charAt(i);
            var isLower = ch === ch.toLowerCase() && ch !== ch.toUpperCase();

            // Background
            var bg = BG;
            var underlineColor = BG;
            if (highlightIdx >= 0) {
                var hLeft = highlightIdx - left;
                var hRight = highlightIdx + right;
                if (i >= hLeft && i < hRight && i >= 0 && i < seqLen) {
                    bg = highlightColor;
                    underlineColor = (i === highlightIdx) ? "#000" : highlightColor;
                }
            }
            ctx.fillStyle = bg;
            ctx.fillRect(x, y, RECT_W, 20);

            // Letter
            ctx.fillStyle = isLower ? "#ff0000" : "#000000";
            ctx.textAlign = "left";
            ctx.fillText(ch.toUpperCase(), x, y + 2);

            // Underline
            ctx.fillStyle = underlineColor;
            ctx.fillRect(x, y + 18, RECT_W - 1, 2);
        }
    }

    function drawStructure() {
        var y = layout.structure.y;
        for (var i = 0; i < seqLen; i++) {
            var x = LABEL_W + i * RECT_W;
            var s = d.structureArray[i];
            if (s !== " ") {
                var c = structureColor(s);
                if (c) {
                    ctx.fillStyle = c;
                    ctx.fillRect(x, y + 4, RECT_W, 15);
                }
            }
        }
    }

    function drawHydropathy() {
        var y = layout.hydropathy.y;
        var h = layout.hydropathy.h;
        for (var i = 0; i < seqLen; i++) {
            var x = LABEL_W + i * RECT_W;
            ctx.fillStyle = kdColor(d.arrayKDHeatMap[i]);
            ctx.fillRect(x, y, RECT_W, h);
        }
    }

    function drawSeparator(y, h) {
        ctx.fillStyle = BG;
        ctx.fillRect(LABEL_W, y, seqLen * RECT_W, h);
    }

    function drawHeatRow(arr, y, h, colorFn) {
        for (var i = 0; i < seqLen; i++) {
            var x = LABEL_W + i * RECT_W;
            ctx.fillStyle = colorFn(arr[i]);
            ctx.fillRect(x, y, RECT_W, h);
        }
    }

    function drawRegionSiteRows(highlightIdx, highlightColor) {
        for (var r = 0; r < regionSiteRows.length; r++) {
            var rowY = layout.regionSiteStart + r * 20;
            var rsIdx = regionSiteRows[r].index;
            for (var i = 0; i < seqLen; i++) {
                var x = LABEL_W + i * RECT_W;
                var rsVal = d.arrayRegionSite[i + rsIdx * seqLen];
                if (rsVal === undefined) rsVal = " ";
                // Convert tab to br for display but draw just the bar
                var displayVal = rsVal.replace(/\t/g, "\n");

                // Highlight background
                var bg = BG;
                if (highlightIdx >= 0) {
                    var hLeft = highlightIdx - left;
                    var hRight = highlightIdx + right;
                    if (i >= hLeft && i < hRight && i >= 0 && i < seqLen) {
                        bg = highlightColor;
                    }
                }
                ctx.fillStyle = bg;
                ctx.fillRect(x, rowY, RECT_W, 20);

                if (d.hasTM && displayVal === "Transmembrane region") {
                    ctx.fillStyle = "#ff0000";
                    ctx.fillRect(x, rowY + 8, RECT_W, 7);
                } else if (displayVal.trim() !== "") {
                    ctx.fillStyle = hashColor(displayVal);
                    ctx.fillRect(x, rowY + 8, RECT_W, 7);
                } else {
                    // Empty: draw thin baseline
                    ctx.fillStyle = "#000";
                    ctx.fillRect(x, rowY + 11, RECT_W, 1);
                }
            }
        }
    }

    // --- Interaction ---
    function getResidueIndex(e) {
        var rect = canvas.getBoundingClientRect();
        var mx = e.clientX - rect.left;
        var my = e.clientY - rect.top;
        var idx = Math.floor((mx - LABEL_W) / RECT_W);
        if (idx < 0 || idx >= seqLen || mx < LABEL_W) return -1;
        return idx;
    }

    function getRowType(e) {
        var rect = canvas.getBoundingClientRect();
        var my = e.clientY - rect.top;
        if (my >= layout.structure.y && my < layout.structure.y + layout.structure.h) return "structure";
        if (my >= layout.hydropathy.y && my < layout.hydropathy.y + layout.hydropathy.h) return "hydropathy";
        if (my >= layout.igScore.y && my < layout.igScore.y + layout.igScore.h) return "igScore";
        if (my >= layout.uniqueness.y && my < layout.uniqueness.y + layout.uniqueness.h) return "uniqueness";
        if (my >= layout.conservation.y && my < layout.conservation.y + layout.conservation.h) return "conservation";
        if (my >= layout.residue.y && my < layout.residue.y + layout.residue.h) return "residue";
        for (var r = 0; r < regionSiteRows.length; r++) {
            var ry = layout.regionSiteStart + r * 20;
            if (my >= ry && my < ry + 20) return "regionSite:" + r;
        }
        return null;
    }

    function buildTooltip(idx, rowType) {
        if (idx < 0 || idx >= seqLen) return null;
        var pep = d.peptide[idx];
        if (!pep && rowType !== "residue" && rowType !== "structure") return null;

        var first = idx - left + 1;
        var last = idx + right;

        if (rowType === "igScore") {
            if (!pep) return null;
            var val = Math.round(d.arrIgScore[idx] * 100) / 100;
            return pep + " " + first + "-" + last + "\nvalue: " + val + "\nrank: " + d.igScoreRank[idx];
        }
        if (rowType === "hydropathy") {
            if (!pep) return null;
            return pep + " " + first + "-" + last + "\nvalue: " + d.arrayKD[idx];
        }
        if (rowType === "uniqueness") {
            if (!pep) return null;
            return d.sortedAllUniquenessMatchList[idx].replace(/<br>/gi, "\n").replace(/<[^>]*>/g, "");
        }
        if (rowType === "conservation") {
            if (!pep) return null;
            return d.sortedAllConservationMatchList[idx].replace(/<br>/gi, "\n").replace(/<[^>]*>/g, "");
        }
        if (rowType === "structure") {
            var s = d.structureArray[idx];
            return (s && s.trim()) ? s : null;
        }
        if (rowType === "residue") {
            var ch = d.segmaskerChar.charAt(idx);
            if (ch === ch.toLowerCase() && ch !== ch.toUpperCase()) {
                return "Low-complexity sequence";
            }
            return null;
        }
        if (rowType && rowType.indexOf("regionSite:") === 0) {
            var rIdx = parseInt(rowType.split(":")[1], 10);
            var rsRowIdx = regionSiteRows[rIdx].index;
            var rsVal = d.arrayRegionSite[idx + rsRowIdx * seqLen];
            if (rsVal && rsVal.trim()) {
                // Original applet wraps regionSite in <html><body>...</body></html>
                // HTML collapses tabs to spaces and wraps text naturally
                return rsVal.replace(/<br>/gi, "\n").replace(/<[^>]*>/g, "").replace(/\t/g, " ");
            }
            return null;
        }
        return null;
    }

    function showTooltip(idx, rowType, text) {
        if (!text) {
            tooltip.style.display = "none";
            return;
        }
        tooltip.textContent = text;
        tooltip.style.display = "block";

        // Region/site tooltips use HTML word-wrapping in the original applet
        // (<html><body>...</body></html>); all others use monospace pre-formatted
        var isRegionSite = rowType && rowType.indexOf("regionSite:") === 0;
        if (isRegionSite) {
            tooltip.style.whiteSpace = "pre";
            tooltip.style.maxWidth = "";
        } else {
            tooltip.style.whiteSpace = "pre";
            tooltip.style.maxWidth = "";
        }

        // Match applet getToolTipLocation: position relative to the hovered component
        // Offset X = (spanSetting * RECT_WIDTH * -1 / 2) - 1  for odd span
        //          = (spanSetting * RECT_WIDTH * -1 / 2) + 3  for even span
        var offsetX;
        if (span % 2 === 1) {
            offsetX = Math.trunc(span * RECT_W * -1 / 2) - 1;
        } else {
            offsetX = Math.trunc(span * RECT_W * -1 / 2) + 3;
        }
        var tipX = LABEL_W + idx * RECT_W + offsetX;
        var tipY = getRowBottom(rowType);
        tooltip.style.left = tipX + "px";
        tooltip.style.top = tipY + "px";
    }

    function getRowBottom(rowType) {
        if (rowType === "structure") return layout.structure.y + layout.structure.h;
        if (rowType === "hydropathy") return layout.hydropathy.y + layout.hydropathy.h;
        if (rowType === "igScore") return layout.igScore.y + layout.igScore.h;
        if (rowType === "uniqueness") return layout.uniqueness.y + layout.uniqueness.h;
        if (rowType === "conservation") return layout.conservation.y + layout.conservation.h;
        if (rowType === "residue") return layout.residue.y + layout.residue.h;
        if (rowType && rowType.indexOf("regionSite:") === 0) {
            var rIdx = parseInt(rowType.split(":")[1], 10);
            return layout.regionSiteStart + (rIdx + 1) * 20;
        }
        return 0;
    }

    canvas.addEventListener("mousemove", function (e) {
        if (frozen) return;
        var idx = getResidueIndex(e);
        var rowType = getRowType(e);
        if (idx >= 0 && d.peptide[idx]) {
            draw(idx, "#ffff00");
        } else {
            draw(-1, null);
        }
        var tip = buildTooltip(idx, rowType);
        showTooltip(idx, rowType, tip);
    });

    canvas.addEventListener("mouseleave", function () {
        if (!frozen) {
            draw(-1, null);
            tooltip.style.display = "none";
        }
    });

    canvas.addEventListener("click", function (e) {
        var idx = getResidueIndex(e);
        if (idx < 0) return;

        if (frozen && frozenIdx === idx) {
            // Unfreeze
            frozen = false;
            frozenIdx = -1;
            draw(-1, null);
            tooltip.style.display = "none";
        } else if (d.peptide[idx]) {
            // Freeze
            frozen = true;
            frozenIdx = idx;
            draw(idx, "#c0c0c0");
            var rowType = getRowType(e);
            var tip = buildTooltip(idx, rowType);
            showTooltip(idx, rowType, tip);
        }
    });

    // Context menu
    canvas.addEventListener("contextmenu", function (e) {
        e.preventDefault();
        var idx = getResidueIndex(e);
        if (idx < 0 || !d.peptide[idx]) {
            ctxMenu.style.display = "none";
            return;
        }
        // Freeze on right-click
        frozen = true;
        frozenIdx = idx;
        draw(idx, "#c0c0c0");

        ctxMenu.style.display = "block";
        ctxMenu.style.left = e.clientX + "px";
        ctxMenu.style.top = e.clientY + "px";
        ctxMenu.dataset.peptide = d.peptide[idx].toUpperCase();
    });

    document.addEventListener("click", function () {
        ctxMenu.style.display = "none";
    });

    document.getElementById("ctx-copy").addEventListener("click", function () {
        var pep = ctxMenu.dataset.peptide;
        if (pep) navigator.clipboard.writeText(pep);
        ctxMenu.style.display = "none";
    });

    document.getElementById("ctx-nterm").addEventListener("click", function () {
        var pep = ctxMenu.dataset.peptide;
        if (pep) navigator.clipboard.writeText("C" + pep);
        ctxMenu.style.display = "none";
    });

    document.getElementById("ctx-cterm").addEventListener("click", function () {
        var pep = ctxMenu.dataset.peptide;
        if (pep) navigator.clipboard.writeText(pep + "C");
        ctxMenu.style.display = "none";
    });

    // Export Raw Data
    document.getElementById("exportRawData").addEventListener("click", function () {
        var blob = new Blob([d.outputText], { type: "text/plain" });
        var a = document.createElement("a");
        a.href = URL.createObjectURL(blob);
        a.download = (d.entryName || "abdesigner") + "_raw_data.txt";
        a.click();
        URL.revokeObjectURL(a.href);
    });

    // Export Image
    document.getElementById("exportImage").addEventListener("click", function () {
        var dataUrl = canvas.toDataURL("image/png");
        var a = document.createElement("a");
        a.href = dataUrl;
        a.download = (d.entryName || "abdesigner") + "_heatmap.png";
        a.click();
    });

    // Initial draw
    draw(-1, null);
})();
