package com.example.restaurant.controller;

import com.example.restaurant.common.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/finance")
public class AdminFinanceController {
    private static final List<String> LABOR_KEYS = List.of("managerLabor", "employeeLabor", "partTimeLabor");
    private static final Map<String, String> REVENUE_LABELS = linkedMap(
            "dineIn", "堂食收银",
            "miniapp", "小程序点餐",
            "aiOrder", "AI 点餐",
            "delivery", "外卖渠道"
    );
    private static final Map<String, String> COST_LABELS = linkedMap(
            "food", "食材成本",
            "managerLabor", "店长人力",
            "employeeLabor", "员工人力",
            "partTimeLabor", "兼职人力",
            "rent", "房租",
            "utilities", "水电燃气",
            "marketing", "营销费用",
            "platformFee", "平台手续费",
            "equipment", "设备耗材",
            "other", "其他成本"
    );

    private final JdbcTemplate jdbcTemplate;

    @GetMapping("/overview")
    public Result<Map<String, Object>> overview(@RequestParam(required = false, defaultValue = "all") String year,
                                                @RequestParam(required = false) String monthStart,
                                                @RequestParam(required = false) String monthEnd,
                                                @RequestParam(required = false, defaultValue = "all") String revenueType) {
        String normalizedRevenueType = REVENUE_LABELS.containsKey(revenueType) ? revenueType : "all";
        List<FinanceRecord> allRecords = records();
        List<FinanceRecord> selectedRecords = allRecords.stream()
                .filter(record -> matches(record, year, monthStart, monthEnd))
                .toList();

        List<Map<String, Object>> normalizedRecords = selectedRecords.stream()
                .map(record -> normalize(record, normalizedRevenueType))
                .toList();
        List<Map<String, Object>> allAnnualSummary = groupByYear(allRecords, normalizedRevenueType);
        List<Map<String, Object>> selectedAnnualSummary = groupByYear(selectedRecords, normalizedRevenueType);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("query", Map.of(
                "year", year == null || year.isBlank() ? "all" : year,
                "monthRange", monthStart == null || monthEnd == null ? List.of() : List.of(monthStart, monthEnd),
                "revenueType", normalizedRevenueType
        ));
        result.put("revenueLabels", REVENUE_LABELS);
        result.put("costLabels", COST_LABELS);
        result.put("yearOptions", allRecords.stream().map(FinanceRecord::year).distinct().toList());
        result.put("monthOptions", allRecords.stream().map(FinanceRecord::month).toList());
        result.put("summary", summarize(selectedRecords, normalizedRevenueType));
        result.put("monthlyDetails", normalizedRecords);
        result.put("annualDetails", selectedAnnualSummary);
        result.put("allAnnualSummary", allAnnualSummary);
        result.put("monthlyTrend", normalizedRecords.stream().map(this::monthlyTrend).toList());
        result.put("annualTrend", allAnnualSummary.stream().map(this::annualTrend).toList());
        result.put("laborMonthlyTrend", normalizedRecords.stream().map(this::laborMonthlyTrend).toList());
        result.put("laborAnnualTrend", allAnnualSummary.stream().map(this::laborAnnualTrend).toList());
        return Result.success(result);
    }

    private List<FinanceRecord> records() {
        return jdbcTemplate.query("""
                SELECT record_month AS month,
                       dine_in_revenue AS dineIn,
                       miniapp_revenue AS miniapp,
                       ai_order_revenue AS aiOrder,
                       delivery_revenue AS delivery,
                       food_cost AS food,
                       manager_labor_cost AS managerLabor,
                       employee_labor_cost AS employeeLabor,
                       part_time_labor_cost AS partTimeLabor,
                       rent_cost AS rent,
                       utilities_cost AS utilities,
                       marketing_cost AS marketing,
                       platform_fee AS platformFee,
                       equipment_cost AS equipment,
                       other_cost AS otherCost
                FROM finance_record
                ORDER BY record_month
                """, (rs, rowNum) -> {
            Map<String, BigDecimal> revenue = new LinkedHashMap<>();
            revenue.put("dineIn", money(rs.getBigDecimal("dineIn")));
            revenue.put("miniapp", money(rs.getBigDecimal("miniapp")));
            revenue.put("aiOrder", money(rs.getBigDecimal("aiOrder")));
            revenue.put("delivery", money(rs.getBigDecimal("delivery")));

            Map<String, BigDecimal> cost = new LinkedHashMap<>();
            cost.put("food", money(rs.getBigDecimal("food")));
            cost.put("managerLabor", money(rs.getBigDecimal("managerLabor")));
            cost.put("employeeLabor", money(rs.getBigDecimal("employeeLabor")));
            cost.put("partTimeLabor", money(rs.getBigDecimal("partTimeLabor")));
            cost.put("rent", money(rs.getBigDecimal("rent")));
            cost.put("utilities", money(rs.getBigDecimal("utilities")));
            cost.put("marketing", money(rs.getBigDecimal("marketing")));
            cost.put("platformFee", money(rs.getBigDecimal("platformFee")));
            cost.put("equipment", money(rs.getBigDecimal("equipment")));
            cost.put("other", money(rs.getBigDecimal("otherCost")));

            String month = rs.getString("month");
            return new FinanceRecord(month, month.substring(0, 4), revenue, cost);
        });
    }

    private boolean matches(FinanceRecord record, String year, String monthStart, String monthEnd) {
        boolean yearMatches = year == null || year.isBlank() || Objects.equals(year, "all") || record.year().equals(year);
        boolean startMatches = monthStart == null || monthStart.isBlank() || record.month().compareTo(monthStart) >= 0;
        boolean endMatches = monthEnd == null || monthEnd.isBlank() || record.month().compareTo(monthEnd) <= 0;
        return yearMatches && startMatches && endMatches;
    }

    private Map<String, Object> normalize(FinanceRecord record, String revenueType) {
        BigDecimal totalRevenue = total(record.revenue());
        BigDecimal queriedRevenue = revenue(record, revenueType);
        BigDecimal totalCost = total(record.cost());
        BigDecimal laborCost = laborCost(record);
        BigDecimal foodCost = value(record.cost(), "food");
        BigDecimal operatingCost = totalCost.subtract(foodCost).subtract(laborCost);
        BigDecimal profit = totalRevenue.subtract(totalCost);

        Map<String, Object> item = new LinkedHashMap<>();
        item.put("month", record.month());
        item.put("year", record.year());
        item.put("revenue", record.revenue());
        item.put("cost", record.cost());
        item.put("totalRevenue", money(totalRevenue));
        item.put("queriedRevenue", money(queriedRevenue));
        item.put("foodCost", money(foodCost));
        item.put("laborCost", money(laborCost));
        item.put("operatingCost", money(operatingCost));
        item.put("totalCost", money(totalCost));
        item.put("profit", money(profit));
        item.put("profitRate", rate(profit, totalRevenue));
        return item;
    }

    private List<Map<String, Object>> groupByYear(List<FinanceRecord> records, String revenueType) {
        Map<String, AnnualAccumulator> grouped = new LinkedHashMap<>();
        for (FinanceRecord record : records) {
            AnnualAccumulator item = grouped.computeIfAbsent(record.year(), AnnualAccumulator::new);
            item.revenue = item.revenue.add(total(record.revenue()));
            item.queriedRevenue = item.queriedRevenue.add(revenue(record, revenueType));
            item.foodCost = item.foodCost.add(value(record.cost(), "food"));
            item.managerLabor = item.managerLabor.add(value(record.cost(), "managerLabor"));
            item.employeeLabor = item.employeeLabor.add(value(record.cost(), "employeeLabor"));
            item.partTimeLabor = item.partTimeLabor.add(value(record.cost(), "partTimeLabor"));
            item.laborCost = item.laborCost.add(laborCost(record));
            item.totalCost = item.totalCost.add(total(record.cost()));
        }

        List<Map<String, Object>> result = new ArrayList<>();
        for (AnnualAccumulator item : grouped.values()) {
            BigDecimal operatingCost = item.totalCost.subtract(item.foodCost).subtract(item.laborCost);
            BigDecimal profit = item.revenue.subtract(item.totalCost);

            Map<String, Object> row = new LinkedHashMap<>();
            row.put("year", item.year);
            row.put("revenue", money(item.revenue));
            row.put("queriedRevenue", money(item.queriedRevenue));
            row.put("foodCost", money(item.foodCost));
            row.put("laborCost", money(item.laborCost));
            row.put("operatingCost", money(operatingCost));
            row.put("totalCost", money(item.totalCost));
            row.put("profit", money(profit));
            row.put("managerLabor", money(item.managerLabor));
            row.put("employeeLabor", money(item.employeeLabor));
            row.put("partTimeLabor", money(item.partTimeLabor));
            row.put("profitRate", rate(profit, item.revenue));
            result.add(row);
        }
        return result;
    }

    private Map<String, Object> summarize(List<FinanceRecord> records, String revenueType) {
        BigDecimal selectedRevenue = sum(records, record -> revenue(record, revenueType));
        BigDecimal totalRevenue = sum(records, record -> total(record.revenue()));
        BigDecimal totalCost = sum(records, record -> total(record.cost()));
        BigDecimal foodCost = sum(records, record -> value(record.cost(), "food"));
        BigDecimal laborCost = sum(records, this::laborCost);
        BigDecimal operatingCost = totalCost.subtract(foodCost).subtract(laborCost);
        BigDecimal profit = totalRevenue.subtract(totalCost);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("selectedRevenue", money(selectedRevenue));
        result.put("totalRevenue", money(totalRevenue));
        result.put("totalCost", money(totalCost));
        result.put("foodCost", money(foodCost));
        result.put("laborCost", money(laborCost));
        result.put("operatingCost", money(operatingCost));
        result.put("profit", money(profit));
        result.put("profitRate", rate(profit, totalRevenue));
        result.put("costRate", rate(totalCost, totalRevenue));
        result.put("foodCostRate", rate(foodCost, totalRevenue));
        result.put("laborCostRate", rate(laborCost, totalRevenue));
        result.put("costStructure", structure(COST_LABELS, records, true));
        result.put("laborStructure", structure(COST_LABELS.entrySet().stream()
                .filter(entry -> LABOR_KEYS.contains(entry.getKey()))
                .collect(LinkedHashMap::new, (map, entry) -> map.put(entry.getKey(), entry.getValue()), LinkedHashMap::putAll),
                records, true));
        result.put("revenueStructure", structure(REVENUE_LABELS, records, false));
        return result;
    }

    private Map<String, Object> monthlyTrend(Map<String, Object> record) {
        Map<String, Object> item = new LinkedHashMap<>();
        item.put("month", record.get("month"));
        item.put("revenue", record.get("totalRevenue"));
        item.put("selectedRevenue", record.get("queriedRevenue"));
        item.put("foodCost", record.get("foodCost"));
        item.put("laborCost", record.get("laborCost"));
        item.put("operatingCost", record.get("operatingCost"));
        item.put("totalCost", record.get("totalCost"));
        item.put("profit", record.get("profit"));
        item.put("profitRate", record.get("profitRate"));
        return item;
    }

    private Map<String, Object> annualTrend(Map<String, Object> record) {
        Map<String, Object> item = new LinkedHashMap<>();
        item.put("year", record.get("year"));
        item.put("revenue", record.get("revenue"));
        item.put("selectedRevenue", record.get("queriedRevenue"));
        item.put("foodCost", record.get("foodCost"));
        item.put("laborCost", record.get("laborCost"));
        item.put("operatingCost", record.get("operatingCost"));
        item.put("totalCost", record.get("totalCost"));
        item.put("profit", record.get("profit"));
        item.put("profitRate", record.get("profitRate"));
        return item;
    }

    private Map<String, Object> laborMonthlyTrend(Map<String, Object> record) {
        @SuppressWarnings("unchecked")
        Map<String, BigDecimal> cost = (Map<String, BigDecimal>) record.get("cost");
        Map<String, Object> item = new LinkedHashMap<>();
        item.put("month", record.get("month"));
        item.put("managerLabor", value(cost, "managerLabor"));
        item.put("employeeLabor", value(cost, "employeeLabor"));
        item.put("partTimeLabor", value(cost, "partTimeLabor"));
        item.put("laborCost", record.get("laborCost"));
        return item;
    }

    private Map<String, Object> laborAnnualTrend(Map<String, Object> record) {
        Map<String, Object> item = new LinkedHashMap<>();
        item.put("year", record.get("year"));
        item.put("managerLabor", record.get("managerLabor"));
        item.put("employeeLabor", record.get("employeeLabor"));
        item.put("partTimeLabor", record.get("partTimeLabor"));
        item.put("laborCost", record.get("laborCost"));
        return item;
    }

    private List<Map<String, Object>> structure(Map<String, String> labels, List<FinanceRecord> records, boolean cost) {
        return labels.entrySet().stream()
                .map(entry -> {
                    Map<String, Object> item = new LinkedHashMap<>();
                    item.put("key", entry.getKey());
                    item.put("name", entry.getValue());
                    item.put("value", money(sum(records, record -> cost ? value(record.cost(), entry.getKey()) : value(record.revenue(), entry.getKey()))));
                    return item;
                })
                .toList();
    }

    private BigDecimal revenue(FinanceRecord record, String revenueType) {
        return "all".equals(revenueType) ? total(record.revenue()) : value(record.revenue(), revenueType);
    }

    private BigDecimal laborCost(FinanceRecord record) {
        return LABOR_KEYS.stream().map(key -> value(record.cost(), key)).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal total(Map<String, BigDecimal> values) {
        return values.values().stream().reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal value(Map<String, BigDecimal> values, String key) {
        return values.getOrDefault(key, BigDecimal.ZERO);
    }

    private BigDecimal sum(List<FinanceRecord> records, FinanceValueGetter getter) {
        return records.stream().map(getter::get).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private static BigDecimal money(BigDecimal value) {
        return (value == null ? BigDecimal.ZERO : value).setScale(2, RoundingMode.HALF_UP);
    }

    private static int rate(BigDecimal numerator, BigDecimal denominator) {
        if (denominator == null || denominator.compareTo(BigDecimal.ZERO) == 0) {
            return 0;
        }
        return numerator.multiply(BigDecimal.valueOf(100)).divide(denominator, 0, RoundingMode.HALF_UP).intValue();
    }

    private static Map<String, String> linkedMap(String... values) {
        Map<String, String> map = new LinkedHashMap<>();
        for (int i = 0; i < values.length; i += 2) {
            map.put(values[i], values[i + 1]);
        }
        return map;
    }

    private record FinanceRecord(String month, String year, Map<String, BigDecimal> revenue, Map<String, BigDecimal> cost) {
    }

    private interface FinanceValueGetter {
        BigDecimal get(FinanceRecord record);
    }

    private static class AnnualAccumulator {
        private final String year;
        private BigDecimal revenue = BigDecimal.ZERO;
        private BigDecimal queriedRevenue = BigDecimal.ZERO;
        private BigDecimal foodCost = BigDecimal.ZERO;
        private BigDecimal laborCost = BigDecimal.ZERO;
        private BigDecimal totalCost = BigDecimal.ZERO;
        private BigDecimal managerLabor = BigDecimal.ZERO;
        private BigDecimal employeeLabor = BigDecimal.ZERO;
        private BigDecimal partTimeLabor = BigDecimal.ZERO;

        private AnnualAccumulator(String year) {
            this.year = year;
        }
    }
}
