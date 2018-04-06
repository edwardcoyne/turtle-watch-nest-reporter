package com.islandturtlewatch.nest.reporter.frontend.reports;

import com.google.common.base.Preconditions;
import com.islandturtlewatch.nest.reporter.frontend.reports.ReportCsvGenerator.Path;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import static com.islandturtlewatch.nest.reporter.frontend.reports.OrderedReportWriter.*;
import static com.islandturtlewatch.nest.reporter.frontend.reports.ReportCsvGenerator.*;

/**
 * Created by ReverendCode on 4/19/17.
 */

public class FetcherBuilder {
    private Map<String, String> modifiers; //from -> to
    private boolean asTimestamp;
    private boolean existenceCheck;
    private String staticValue;
    private String emptyString;
    private String extantString;
    private String extinctString;
    private DateFormat DATE_FORMAT;

    private Path displayPath;
    private Path comparisonPath;

    public FetcherBuilder() {
        this.emptyString = "";
        this.asTimestamp = false;
        this.existenceCheck = false;
        this.staticValue = null; //to check later?
    }

    public FetcherBuilder addDisplayPath(String stringPath) {
        this.displayPath = new Path(stringPath);
        return this;
    }

    public FetcherBuilder addComparisonPath(String stringPath) {
        this.comparisonPath = new Path(stringPath);
        return this;
    }

    public FetcherBuilder sub(String from, String to) {
        this.modifiers.put(from, to);
        return this;
    }

    public FetcherBuilder changeTimestampFormat(String newFormat) {
        this.DATE_FORMAT = new SimpleDateFormat(newFormat);
        return this;
    }
    public FetcherBuilder changeNullResponse(String newValue) {
        this.emptyString = newValue;
        return this;
    }

    public FetcherBuilder setStaticValue(String staticValue) {
        this.staticValue = staticValue;
        return this;
    }

    public FetcherBuilder existenceValues(String exists, String notExist) {
        this.extantString = exists;
        this.extinctString = notExist;
        this.existenceCheck = true;
        return this;
    }

    public FetcherBuilder asTimestamp(String dateFormat) {
        this.DATE_FORMAT = new SimpleDateFormat(dateFormat);
        this.asTimestamp = true;
        return this;
    }
//    TODO: We need to enforce some rules for when a build is allowed to happen
    public ValueFetcher build() {
        return new ValueFetcher() {
            @Override
            public String fetch(Map<Path, Column> columnMap, int rowId) {
                if (staticValue != null) {
                    return staticValue;
                }
                Column displayColumn = columnMap.get(displayPath);
                if (existenceCheck) {
//                    return whether or not there is a value at displayValue
                    if (displayColumn == null) return extinctString;
                    else return displayColumn.hasValue(rowId) ? extantString : extinctString;
                } else if (asTimestamp) {
                    Preconditions.checkNotNull(displayColumn, "Missing path: " + displayPath.toString());
                    if (!displayColumn.hasValue(rowId) || displayColumn.getValue(rowId).equals("0")) {
                        return emptyString;
                    }
                    Long timestamp = Long.parseLong(displayColumn.getValue(rowId));
                    return DATE_FORMAT.format(new Date(timestamp));
                }
                return modifiers.containsKey(displayColumn.getValue(rowId)) ?
                        modifiers.get(displayColumn.getValue(rowId)) : displayColumn.getValue(rowId);
            }
        };
    }

}
