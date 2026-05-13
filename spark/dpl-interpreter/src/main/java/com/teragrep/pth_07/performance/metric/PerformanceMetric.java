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
package com.teragrep.pth_07.performance.metric;

import com.codahale.metrics.Metric;
import com.teragrep.pth_07.performance.metric.value.MetricValue;
import com.teragrep.pth_07.performance.metric.value.MetricValueImpl;
import com.teragrep.zep_01.common.exception.IncompatibleValueException;
import org.apache.spark.sql.types.DataType;
import org.apache.spark.sql.types.DataTypes;
import org.apache.spark.sql.types.Metadata;
import org.apache.spark.sql.types.StructField;

import java.util.Objects;

public final class PerformanceMetric {

    private final MetricValue value;
    private final String name;
    private final DataType type;
    private final Metadata metadata;
    private final boolean nullable;

    public PerformanceMetric(final MetricValue value, final String name, final DataType type, final Metadata metadata, final boolean nullable){
        this.value = value;
        this.name = name;
        this.type = type;
        this.metadata = metadata;
        this.nullable = nullable;
    }
    public MetricValue value(){
        return value;
    }

    public PerformanceMetric withValue(final long value) {
        return new PerformanceMetric(new MetricValueImpl(value, DataTypes.LongType),this.name,this.type,this.metadata,this.nullable);
    }
    public PerformanceMetric withValue(final double value) {
        return new PerformanceMetric(new MetricValueImpl(value, DataTypes.DoubleType),this.name,this.type,this.metadata,this.nullable);
    }

    public String name() {
        return name;
    }

    public StructField toStructField() {
        final StructField structField = new StructField(name,type,nullable,metadata);
        return structField;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PerformanceMetric metric = (PerformanceMetric) o;
        return nullable == metric.nullable && Objects.equals(value, metric.value) && Objects.equals(name, metric.name) && Objects.equals(type, metric.type) && Objects.equals(metadata, metric.metadata);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value, name, type, metadata, nullable);
    }
}
