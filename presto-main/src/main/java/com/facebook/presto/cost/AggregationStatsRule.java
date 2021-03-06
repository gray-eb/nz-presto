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
package com.facebook.presto.cost;

import com.facebook.presto.Session;
import com.facebook.presto.matching.Pattern;
import com.facebook.presto.spi.type.Type;
import com.facebook.presto.sql.planner.Symbol;
import com.facebook.presto.sql.planner.iterative.Lookup;
import com.facebook.presto.sql.planner.plan.AggregationNode;
import com.facebook.presto.sql.planner.plan.AggregationNode.Aggregation;
import com.facebook.presto.sql.planner.plan.PlanNode;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;

import static com.facebook.presto.util.MoreMath.isPositiveOrNan;
import static com.google.common.collect.Iterables.getOnlyElement;
import static java.lang.Math.min;
import static java.util.Objects.requireNonNull;

public class AggregationStatsRule
        implements ComposableStatsCalculator.Rule
{
    private static final Pattern PATTERN = Pattern.matchByClass(AggregationNode.class);

    @Override
    public Pattern getPattern()
    {
        return PATTERN;
    }

    @Override
    public Optional<PlanNodeStatsEstimate> calculate(PlanNode node, Lookup lookup, Session session, Map<Symbol, Type> types)
    {
        AggregationNode aggregationNode = (AggregationNode) node;

        if (aggregationNode.getGroupingSets().size() != 1) {
            return Optional.empty();
        }

        return Optional.of(groupBy(
                lookup.getStats(aggregationNode.getSource(), session, types),
                getOnlyElement(aggregationNode.getGroupingSets()),
                aggregationNode.getAggregations()));
    }

    public static PlanNodeStatsEstimate groupBy(PlanNodeStatsEstimate input, Collection<Symbol> groupBySymbols, Map<Symbol, Aggregation> aggregations)
    {
        PlanNodeStatsEstimate.Builder result = PlanNodeStatsEstimate.builder();
        for (Symbol groupBySymbol : groupBySymbols) {
            SymbolStatsEstimate symbolStatistics = input.getSymbolStatistics(groupBySymbol);
            result.addSymbolStatistics(groupBySymbol, symbolStatistics.mapNullsFraction(nullsFraction -> {
                if (isPositiveOrNan(nullsFraction)) {
                    double distinctValuesCount = symbolStatistics.getDistinctValuesCount();
                    return 1.0 / (distinctValuesCount + 1);
                }
                return 0.0;
            }));
        }

        double rowsCount = 1;
        for (Symbol groupBySymbol : groupBySymbols) {
            SymbolStatsEstimate symbolStatistics = input.getSymbolStatistics(groupBySymbol);
            int nullRow = isPositiveOrNan(symbolStatistics.getNullsFraction()) ? 1 : 0;
            rowsCount *= symbolStatistics.getDistinctValuesCount() + nullRow;
        }
        result.setOutputRowCount(min(rowsCount, input.getOutputRowCount()));

        for (Map.Entry<Symbol, Aggregation> aggregationEntry : aggregations.entrySet()) {
            result.addSymbolStatistics(aggregationEntry.getKey(), estimateAggregationStats(aggregationEntry.getValue(), input));
        }

        return result.build();
    }

    private static SymbolStatsEstimate estimateAggregationStats(Aggregation aggregation, PlanNodeStatsEstimate sourceStats)
    {
        requireNonNull(aggregation, "aggregation is null");
        requireNonNull(sourceStats, "sourceStats is null");

        // TODO implement simple aggregations like: min, max, count, sum
        return SymbolStatsEstimate.UNKNOWN_STATS;
    }
}
