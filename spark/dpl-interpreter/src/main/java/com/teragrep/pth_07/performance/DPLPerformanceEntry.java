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
package com.teragrep.pth_07.performance;

import com.teragrep.pth_07.performance.metric.*;
import com.teragrep.pth_07.performance.metric.value.MetricValue;
import com.teragrep.pth_07.performance.metric.value.StubMetricValue;
import com.teragrep.zep_01.common.exception.IncompatibleValueException;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.catalyst.expressions.GenericRowWithSchema;
import org.apache.spark.sql.types.*;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class DPLPerformanceEntry {
    private final Map<String,PerformanceMetric> metrics;

    /**
     * DPLPerformanceEntry represents a row of performance data from a DPL query. Each batch received from DPL corresponds to one DPLPerformanceEntry.
     * DPLPerformanceEntry keeps a map of PerformanceMetrics, each of which can be updated using the withData() methods.
     * DPLPerformanceEntry can produce a Spark schema that contains all the information for each of the PerformanceMetrics it contains.
     * DPLPerformanceEntry is capable of turning itself into a Spark Row that can be added to a Spark Dataset.
     */
    public DPLPerformanceEntry(){
        this(Stream.of(
                        new AbstractMap.SimpleEntry<>("ArchiveCompressedBytesProcessed: total compressed bytes processed from archive",new PerformanceMetric(new StubMetricValue(),"ArchiveCompressedBytesProcessed: total compressed bytes processed from archive",DataTypes.LongType, Metadata.empty(),false)),
                        new AbstractMap.SimpleEntry<>("ArchiveDatabaseRowAvgLatency: average time per row in nanoseconds",new PerformanceMetric(new StubMetricValue(),"ArchiveDatabaseRowAvgLatency: average time per row in nanoseconds",DataTypes.LongType,Metadata.empty(),false)),
                        new AbstractMap.SimpleEntry<>("ArchiveDatabaseRowCount: number of processed archive database rows",new PerformanceMetric(new StubMetricValue(),"ArchiveDatabaseRowCount: number of processed archive database rows",DataTypes.LongType,Metadata.empty(),false)),
                        new AbstractMap.SimpleEntry<>("ArchiveDatabaseRowMaxLatency: maximum time per row in nanoseconds",new PerformanceMetric(new StubMetricValue(),"ArchiveDatabaseRowMaxLatency: maximum time per row in nanoseconds",DataTypes.LongType,Metadata.empty(),false)),
                        new AbstractMap.SimpleEntry<>("ArchiveDatabaseRowMinLatency: minimum time per row in nanoseconds",new PerformanceMetric(new StubMetricValue(),"ArchiveDatabaseRowMinLatency: minimum time per row in nanoseconds",DataTypes.LongType,Metadata.empty(),false)),
                        new AbstractMap.SimpleEntry<>("ArchiveObjectsProcessed: total objects processed from archive",new PerformanceMetric(new StubMetricValue(),"ArchiveObjectsProcessed: total objects processed from archive",DataTypes.LongType,Metadata.empty(),false)),
                        new AbstractMap.SimpleEntry<>("ArchiveOffset: latest archive offset processed (epoch time)",new PerformanceMetric(new StubMetricValue(),"ArchiveOffset: latest archive offset processed (epoch time)",DataTypes.LongType,Metadata.empty(),false)),
                        new AbstractMap.SimpleEntry<>("BatchId: sequence number of the batch",new PerformanceMetric(new StubMetricValue(),"BatchId: sequence number of the batch",DataTypes.LongType,Metadata.empty(),false)),
                        new AbstractMap.SimpleEntry<>("BytesPerSecond: processed bytes per second",new PerformanceMetric(new StubMetricValue(),"BytesPerSecond: processed bytes per second",DataTypes.LongType,Metadata.empty(),false)),
                        new AbstractMap.SimpleEntry<>("BytesProcessed: total bytes processed",new PerformanceMetric(new StubMetricValue(),"BytesProcessed: total bytes processed",DataTypes.LongType,Metadata.empty(),false)),
                        new AbstractMap.SimpleEntry<>("Eps: processed rows per second",new PerformanceMetric(new StubMetricValue(),"Eps: processed rows per second",DataTypes.DoubleType,Metadata.empty(),false)),
                        new AbstractMap.SimpleEntry<>("KafkaOffset: sum of processed kafka offsets",new PerformanceMetric(new StubMetricValue(),"KafkaOffset: sum of processed kafka offsets",DataTypes.LongType,Metadata.empty(),false)),
                        new AbstractMap.SimpleEntry<>("LatestKafkaTimestamp: latest processed kafka records' timestamp",new PerformanceMetric(new StubMetricValue(),"LatestKafkaTimestamp: latest processed kafka records' timestamp",DataTypes.LongType,Metadata.empty(),false)),
                        new AbstractMap.SimpleEntry<>("RecordsPerSecond: processed records per second",new PerformanceMetric(new StubMetricValue(),"RecordsPerSecond: processed records per second",DataTypes.LongType,Metadata.empty(),false)),
                        new AbstractMap.SimpleEntry<>("RecordsProcessed: total processed records",new PerformanceMetric(new StubMetricValue(),"RecordsProcessed: total processed records",DataTypes.LongType,Metadata.empty(),false)),
                        new AbstractMap.SimpleEntry<>("RowsReadFromArchive: Full table input rows read from arcihve",new PerformanceMetric(new StubMetricValue(),"RowsReadFromArchive: Full table input rows read from archive",DataTypes.LongType,Metadata.empty(),false)),
                        new AbstractMap.SimpleEntry<>("Timestamp: timestamp of when performance data was received(epochtime)",new PerformanceMetric(new StubMetricValue(),"Timestamp: timestamp of when performance data was received(epochtime)",DataTypes.LongType, new MetadataBuilder().putBoolean("dpl_internal_isGroupByColumn",true).build(),false)))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)));
    }

    public DPLPerformanceEntry(final Map<String,PerformanceMetric> metrics){
        this.metrics = metrics;
    }

    /**
     * Creates a new instance of this entry, with an updated value for the metric identified by key. If given key does not match with any of the metrics, returns this entry without modification.
     * @param key PerformanceMetric to update. Must match with the key of one of the PerformanceMetrics this object was initialized with.
     * @param value new value
     * @return a modified instance of this DPLPerformanceEntry, with the PerformanceMetric identified by key having it's value replaced by the given value.
     */
    public DPLPerformanceEntry withData(final String key, final long value) {
        if(!metrics.containsKey(key)){
            return this;
        }
        PerformanceMetric metric = metrics.get(key);
        PerformanceMetric modifiedMetric = metric.withValue(value);
        Map<String, PerformanceMetric> modifiedMetrics = new HashMap<>(metrics);
        modifiedMetrics.put(key,modifiedMetric);
        return new DPLPerformanceEntry(modifiedMetrics);
    }

    /**
     * Creates a new instance of this entry, with an updated value for the metric identified by key. If given key does not match with any of the metrics, returns this entry without modification.
     * @param key PerformanceMetric to update. Must match with the key of one of the PerformanceMetrics this object was initialized with.
     * @param value new value
     * @return a modified instance of this DPLPerformanceEntry, with the PerformanceMetric identified by key having it's value replaced by the given value.
     */
    public DPLPerformanceEntry withData(final String key, final double value) {
        if(!metrics.containsKey(key)){
            return this;
        }
        PerformanceMetric metric = metrics.get(key);
        PerformanceMetric modifiedMetric = metric.withValue(value);
        Map<String, PerformanceMetric> modifiedMetrics = new HashMap<>(metrics);
        modifiedMetrics.put(key,modifiedMetric);
        return new DPLPerformanceEntry(modifiedMetrics);
    }

    /**
     * Turns the PerformanceMetrics within this entry into a Spark Row.
     * Any PerformanceMetrics containing a StubMetricValue will be represented as a null.
     * The schema of the row corresponds to the given metrics.
     * @return Spark Row object representing this entry
     */
    public Row asRow(){
        return asRow(performanceSchema());
    }

    /**
     * Turns the PerformanceMetrics within this entry that match with the given Schema into a Spark Row.
     * Any PerformanceMetrics containing a StubMetricValue will be represented as a null
     * Any PerformanceMetrics that do not match with the given Schema will be skipped
     * Any Schema fields that don't have a matching PerformanceMetric will be represented as a null
     * @param schema Schema to apply to the Row
     * @return Spark Row object representing this entry
     */
    public Row asRow(final StructType schema){
        final List<Object> values = new ArrayList<>();
        for (final StructField field : schema.fields()) {
            if(metrics.containsKey(field.name()) && !metrics.get(field.name()).value().isStub()){
                final MetricValue metricValue = metrics.get(field.name()).value();
                values.add(metricValue.value());
            }
            else {
                values.add(null);
            }
        }
        return new GenericRowWithSchema(values.toArray(),schema);
    }

    /**
     * Generates a Spark Schema based on the PerformanceMetrics this object was initialized with
     * @return Spark Schema containing information on the metrics defined in this entry
     */
    public StructType performanceSchema(){
        StructType performanceSchema = new StructType();
        for (PerformanceMetric metric : metrics.values()) {
            performanceSchema = performanceSchema.add(metric.toStructField());
        }
        return performanceSchema;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DPLPerformanceEntry entry = (DPLPerformanceEntry) o;
        return Objects.equals(metrics, entry.metrics);
    }

    @Override
    public int hashCode() {
        return Objects.hash(metrics);
    }
}
