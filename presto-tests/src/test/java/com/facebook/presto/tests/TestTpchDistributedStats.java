/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.facebook.presto.tests;

import com.facebook.presto.tests.statistics.StatisticsAssertion;
import com.facebook.presto.tpch.ColumnNaming;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Range;
import io.airlift.tpch.TpchTable;
import org.testng.annotations.Test;

import static com.facebook.presto.tests.statistics.MetricComparisonStrategies.absoluteError;
import static com.facebook.presto.tests.statistics.MetricComparisonStrategies.defaultTolerance;
import static com.facebook.presto.tests.statistics.MetricComparisonStrategies.noError;
import static com.facebook.presto.tests.statistics.MetricComparisonStrategies.relativeError;
import static com.facebook.presto.tests.statistics.Metrics.OUTPUT_ROW_COUNT;
import static com.facebook.presto.tests.tpch.TpchQueryRunner.createQueryRunnerWithoutCatalogs;
import static java.util.Collections.emptyMap;

public class TestTpchDistributedStats
{
    private final StatisticsAssertion statisticsAssertion;

    public TestTpchDistributedStats()
            throws Exception
    {
        DistributedQueryRunner runner = createQueryRunnerWithoutCatalogs(emptyMap(), emptyMap());
        runner.createCatalog("tpch", "tpch", ImmutableMap.of(
                "tpch.column-naming", ColumnNaming.STANDARD.name()
        ));
        statisticsAssertion = new StatisticsAssertion(runner);
    }

    @Test(enabled = false)
    void testTableScanStats()
    {
        TpchTable.getTables()
                .forEach(table -> statisticsAssertion.check("SELECT * FROM " + table.getTableName(),
                        checks -> checks
                                // TODO use noError
                                .estimate(OUTPUT_ROW_COUNT, defaultTolerance())));
    }

    @Test(enabled = false)
    void testFilter()
    {
        String query = "SELECT * FROM lineitem" +
                " WHERE l_shipdate <= DATE '1998-12-01' - INTERVAL '90' DAY";
        statisticsAssertion.check(query,
                checks -> checks
                        .estimate(OUTPUT_ROW_COUNT, relativeError(Range.closed(-.55, -.45))));
    }

    @Test(enabled = false)
    void testJoin()
    {
        statisticsAssertion.check("SELECT * FROM  part, partsupp WHERE p_partkey = ps_partkey",
                checks -> checks
                        .estimate(OUTPUT_ROW_COUNT, relativeError(Range.closed(.95, 1.05))));
    }

    @Test(enabled = false)
    void testSetOperations()
    {
        statisticsAssertion.check("SELECT * FROM nation UNION SELECT * FROM nation",
                checks -> checks
                        .noEstimate(OUTPUT_ROW_COUNT));

        statisticsAssertion.check("SELECT * FROM nation INTERSECT SELECT * FROM nation",
                checks -> checks
                        .noEstimate(OUTPUT_ROW_COUNT));

        statisticsAssertion.check("SELECT * FROM nation EXCEPT SELECT * FROM nation",
                checks -> checks
                        .noEstimate(OUTPUT_ROW_COUNT));
    }

    @Test(enabled = false)
    void testEnforceSingleRow()
    {
        statisticsAssertion.check("SELECT (SELECT n_regionkey FROM nation WHERE n_name = 'Germany')",
                checks -> checks
                        .estimate(OUTPUT_ROW_COUNT, noError()));
    }

    @Test(enabled = false)
    void testValues()
    {
        statisticsAssertion.check("VALUES 1",
                checks -> checks
                        .estimate(OUTPUT_ROW_COUNT, noError()));
    }

    @Test(enabled = false)
    void testSemiJoin()
    {
        statisticsAssertion.check("SELECT * FROM nation WHERE n_regionkey IN (SELECT r_regionkey FROM region)",
                checks -> checks
                        .estimate(OUTPUT_ROW_COUNT, noError()));
        statisticsAssertion.check("SELECT * FROM nation WHERE n_regionkey IN (SELECT r_regionkey FROM region WHERE r_regionkey % 3 = 0)",
                checks -> checks
                        .estimate(OUTPUT_ROW_COUNT, absoluteError(Range.singleton(15.))));
    }

    @Test(enabled = false)
    void testLimit()
    {
        statisticsAssertion.check("SELECT * FROM nation LIMIT 10",
                checks -> checks
                        .estimate(OUTPUT_ROW_COUNT, noError()));
    }

    @Test(enabled = false)
    void testGroupBy()
    {
        String query = "SELECT l_returnflag, l_linestatus FROM lineitem" +
                " GROUP BY l_returnflag, l_linestatus";
        statisticsAssertion.check(query,
                checks -> checks
                        .noEstimate(OUTPUT_ROW_COUNT));
    }
}
