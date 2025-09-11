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

import com.teragrep.pth_07.ui.elements.AbstractUserInterfaceElement;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import com.teragrep.zep_01.display.AngularObject;
import com.teragrep.zep_01.interpreter.InterpreterContext;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public final class DTTableDatasetNg extends AbstractUserInterfaceElement {
    // FIXME Exceptions should cause interpreter to stop

    private final Lock lock = new ReentrantLock();
    private final AngularObject AJAXRequestAngularObject;

    public DTTableDatasetNg(InterpreterContext interpreterContext, AngularObject AJAXRequestAngularObject) {
        super(interpreterContext);
        this.AJAXRequestAngularObject = AJAXRequestAngularObject;
    }

    @Override
    public void draw() {
    }

    @Override
    public void emit() {
        try {
            lock.lock();
            AJAXRequestAngularObject.emit();

        } finally {
            lock.unlock();
        }
    }

    public void setParagraphDataset(Dataset<Row> rowDataset) {
        /*
         TODO check if other presentation can be used than string, for order
         i.e. rowDataset.collectAsList()
         */
        try {
            lock.lock();
            if (rowDataset.schema().nonEmpty()) {
                AJAXRequestAngularObject.clearAllWatchers();
                AJAXRequestAngularObject.addWatcher(new AJAXRequestWatcher(getInterpreterContext(),rowDataset));
            }
        } finally {
            lock.unlock();
        }
    }
}
