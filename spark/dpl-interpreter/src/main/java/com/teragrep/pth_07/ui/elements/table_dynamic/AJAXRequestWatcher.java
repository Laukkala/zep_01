/*
 * Teragrep DPL Spark Integration PTH-07
 * Copyright (C) 2022  Suomen Kanuuna Oy
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://github.com/teragrep/teragrep/blob/main/LICENSE>.
 *
 *
 * Additional permission under GNU Affero General Public License version 3
 * section 7
 *
 * If you modify this Program, or any covered work, by linking or combining it
 * with other code, such other code is not for that reason alone subject to any
 * of the requirements of the GNU Affero GPL version 3 as long as this Program
 * is the same Program as licensed from Suomen Kanuuna Oy without any additional
 * modifications.
 *
 * Supplemented terms under GNU Affero General Public License version 3
 * section 7
 *
 * Origin of the software must be attributed to Suomen Kanuuna Oy. Any modified
 * versions must be marked as "Modified version of" The Program.
 *
 * Names of the licensors and authors may not be used for publicity purposes.
 *
 * No rights are granted for use of trade names, trademarks, or service marks
 * which are in The Program if any.
 *
 * Licensee must indemnify licensors and authors for any liability that these
 * contractual assumptions impose on licensors and authors.
 *
 * To the extent this program is licensed as part of the Commercial versions of
 * Teragrep, the applicable Commercial License may apply to this file if you as
 * a licensee so wish it.
 */
package com.teragrep.pth_07.ui.elements.table_dynamic;

import com.teragrep.zep_01.display.AngularObjectWatcher;
import com.teragrep.zep_01.interpreter.InterpreterContext;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.json.*;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

public class AJAXRequestWatcher extends AngularObjectWatcher {
    private static final Logger LOGGER = LoggerFactory.getLogger(AJAXRequestWatcher.class);
    private final Dataset<Row> rowDataset;

    public AJAXRequestWatcher(InterpreterContext context, Dataset<Row> rowDataset){
        super(context);
        this.rowDataset = rowDataset;
    }
    @Override
    public void watch(Object o, Object o1, InterpreterContext interpreterContext) {
        if(LOGGER.isTraceEnabled()) {
            LOGGER.trace("AJAXRequest <- {}", o.toString());
            LOGGER.trace("AJAXRequest -> {}", o1.toString());
        }
        String requestString = (String) o1;
        try{
            JsonObject ajaxRequest = Json.createReader(new StringReader(requestString)).readObject();
            if(!isValidRequest(ajaxRequest))
            {
                LOGGER.error("AJAXRequestWatcher received invalid JSON data: " + ajaxRequest);
            }
            else {
                JsonObject output = searchAndPaginate(ajaxRequest);
                String outputString = output.toString();
                interpreterContext.out().clear();
                interpreterContext.out().write(outputString);
                interpreterContext.out().flush();
            }
        }
        catch (JsonException jsonException){
            LOGGER.error("AJAXRequestWatcher received unparseable JSON data: " + requestString);
        }
        catch (IOException ioException){
            LOGGER.error("Failed to write to InterpreterOutput: ",ioException);
        }
    }

    private JsonObject searchAndPaginate(JsonObject ajaxRequest){
        int start = ajaxRequest.getJsonNumber("start").intValue();
        int length = ajaxRequest.getJsonNumber("length").intValue();
        int draw = ajaxRequest.getJsonNumber("draw").intValue();
        String searchString = ajaxRequest.getJsonObject("search").getString("value");
        List<String> rowList = rowDataset.toJSON().collectAsList();

        DTHeader dtHeader = new DTHeader(rowDataset.schema());
        JsonArray schemaHeadersJson = dtHeader.json();
        List<String> searchedList = search(rowList,searchString);

        // pagination
        List<String> paginatedList = paginate(searchedList,length,start);

        // ui formatting
        JsonArray jsonFormattedArray = format(paginatedList);

        int recordsTotal = rowList.size();
        int recordsFiltered = searchedList.size();

        JsonObjectBuilder builder = Json.createObjectBuilder();
        builder.add("headers",schemaHeadersJson);
        builder.add("data", jsonFormattedArray);
        builder.add("draw", draw);
        builder.add("recordsTotal", recordsTotal);
        builder.add("recordsFiltered", recordsFiltered);
        return builder.build();
    }

    private boolean isValidRequest(JsonObject ajaxRequest){
        // validate request by checking that every required key exists and that the type of the key is expected.
        return (ajaxRequest.get("draw") != null
                && ajaxRequest.get("draw").getValueType() == JsonValue.ValueType.NUMBER
                && ajaxRequest.get("start") != null
                && ajaxRequest.get("start").getValueType() == JsonValue.ValueType.NUMBER
                && ajaxRequest.get("length") != null
                && ajaxRequest.get("length").getValueType() == JsonValue.ValueType.NUMBER
                && ajaxRequest.get("search") != null
                && ajaxRequest.get("search").getValueType() == JsonValue.ValueType.OBJECT
                && ajaxRequest.getJsonObject("search").get("value") != null
                && ajaxRequest.getJsonObject("search").get("value").getValueType() == JsonValue.ValueType.STRING);
    }

    private JsonArray format(List<String> rowList) {
        try{
            JsonArrayBuilder builder = Json.createArrayBuilder();
            for (String S : rowList) {
                JsonReader reader = Json.createReader(new StringReader(S));
                JsonObject line = reader.readObject();
                builder.add(line);
                reader.close();
            }
            return builder.build();
        }catch(JsonException | IllegalStateException e){
            LOGGER.error(e.toString());
            return(Json.createArrayBuilder().build());
        }
    }

    private List<String> search(List<String> rowList, String searchString){
        List<String> searchedList = new ArrayList<>();
        if (!"".equals(searchString)) {
            try {
                for (String row : rowList) {
                    JsonReader reader = Json.createReader(new StringReader(row));
                    JsonObject line = reader.readObject();

                    // NOTE hard coded to _raw column
                    JsonString _raw = line.getJsonString("_raw");
                    if (_raw != null) {
                        String _rawString = _raw.getString();
                        if (_rawString != null) {
                            if (_rawString.contains(searchString)) {
                                // _raw matches, add whole row to result set
                                searchedList.add(row);
                            }
                        }
                    }
                    reader.close();
                }
                return searchedList;
            } catch (JsonException | IllegalStateException e) {
                LOGGER.error(e.toString());
                return searchedList;
            }
        }
        else {
            searchedList = rowList;
        }
        return searchedList;
    }

    private List<String> paginate(List<String> rowList, int pageSize, int pageStart) {
        // ranges must be greater than 0
        int fromIndex = Math.max(pageStart, 0);
        int toIndex = Math.max(fromIndex + pageSize, 0);

        // list must end at the maximum size
        if (toIndex > rowList.size()) {
            toIndex = rowList.size();
        }

        // list range must be positive
        if (fromIndex > toIndex) {
            fromIndex = toIndex;
        }

        return rowList.subList(fromIndex, toIndex);
    }
}
