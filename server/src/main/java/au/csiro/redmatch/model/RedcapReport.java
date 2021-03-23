/*
 * Copyright © 2018-2021, Commonwealth Scientific and Industrial Research
 * Organisation (CSIRO) ABN 41 687 119 230. Licensed under the CSIRO Open Source
 * Software Licence Agreement.
 */
package au.csiro.redmatch.model;

import java.util.List;

/**
 * Represents a REDCap report and a hash of the raw JSON returned by the API.
 *
 * @author Alejandro Metke Jimenez
 *
 */
public class RedcapReport {

    private List<Row> rows;

    private String hash;

    public RedcapReport(List<Row> rows, String hash) {
        this.rows = rows;
        this.hash = hash;
    }

    public List<Row> getRows() {
        return rows;
    }

    public String getHash() {
        return hash;
    }
}
