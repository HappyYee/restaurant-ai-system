package com.example.restaurant.service.impl;

import com.example.restaurant.service.BusinessStatsService;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BusinessStatsServiceImpl implements BusinessStatsService {
    private static final BigDecimal ZERO = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);

    private final JdbcTemplate jdbcTemplate;

    @Override
    public Map<String, Object> dashboard() {
        LocalDate today = LocalDate.now();
        LocalDate yesterday = today.minusDays(1);
        LocalDateTime todayStart = today.atStartOfDay();
        LocalDateTime tomorrowStart = today.plusDays(1).atStartOfDay();
        LocalDateTime yesterdayStart = yesterday.atStartOfDay();

        BigDecimal revenueToday = revenueBetween(todayStart, tomorrowStart);
        BigDecimal revenueYesterday = revenueBetween(yesterdayStart, todayStart);
        int orderToday = countOrdersBetween(todayStart, tomorrowStart);
        int orderYesterday = countOrdersBetween(yesterdayStart, todayStart);
        BigDecimal foodCostToday = foodCostBetween(todayStart, tomorrowStart);
        BigDecimal laborCostMonth = laborCostMonth();
        BigDecimal laborCostToday = money(laborCostMonth.divide(BigDecimal.valueOf(30), 4, RoundingMode.HALF_UP));
        BigDecimal grossProfitToday = money(revenueToday.subtract(foodCostToday));
        BigDecimal netProfitToday = money(grossProfitToday.subtract(laborCostToday));

        Map<String, Object> currentMonth = currentMonthFinance();
        List<Map<String, Object>> monthlyFinance = monthlyFinance();
        List<Map<String, Object>> annualFinance = annualFinance();

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("revenueToday", revenueToday);
        result.put("revenueYesterday", revenueYesterday);
        result.put("todayRevenue", revenueToday);
        result.put("yesterdayRevenue", revenueYesterday);
        result.put("revenueChange", changeRate(revenueToday, revenueYesterday));
        result.put("foodCostToday", foodCostToday);
        result.put("laborCostToday", laborCostToday);
        result.put("grossProfitToday", grossProfitToday);
        result.put("netProfitToday", netProfitToday);
        result.put("avgTicket", orderToday == 0 ? ZERO : money(revenueToday.divide(BigDecimal.valueOf(orderToday), 4, RoundingMode.HALF_UP)));
        result.put("foodCostRate", rate(foodCostToday, revenueToday));
        result.put("laborCostRate", rate(laborCostToday, revenueToday));
        result.put("profitMargin", rate(netProfitToday, revenueToday));
        result.put("orderToday", orderToday);
        result.put("orderYesterday", orderYesterday);
        result.put("todayOrderCount", orderToday);
        result.put("yesterdayOrderCount", orderYesterday);
        result.put("orderChange", changeRate(BigDecimal.valueOf(orderToday), BigDecimal.valueOf(orderYesterday)));
        result.put("productCount", intValue("SELECT COUNT(*) FROM product WHERE deleted = 0"));
        result.put("activeProductCount", intValue("SELECT COUNT(*) FROM product WHERE deleted = 0 AND status = 1"));
        result.put("lowStockCount", intValue("SELECT COUNT(*) FROM product WHERE deleted = 0 AND stock <= 10"));
        result.put("pendingOrderCount", intValue("""
                SELECT COUNT(*) FROM orders
                WHERE create_time >= ? AND create_time < ? AND status = 0
                """, ts(todayStart), ts(tomorrowStart)));
        result.put("staffCount", intValue("SELECT COUNT(*) FROM staff WHERE status = 1"));
        result.put("memberCount", intValue("SELECT COUNT(*) FROM `user`"));
        result.put("laborCostMonth", laborCostMonth);
        result.put("monthRevenue", currentMonth.get("revenue"));
        result.put("monthExpense", currentMonth.get("expense"));
        result.put("monthProfit", currentMonth.get("profit"));
        result.put("yearRevenue", annualFinance.isEmpty() ? ZERO : annualFinance.get(annualFinance.size() - 1).get("revenue"));
        result.put("yearExpense", annualFinance.isEmpty() ? ZERO : annualFinance.get(annualFinance.size() - 1).get("expense"));
        result.put("yearProfit", annualFinance.isEmpty() ? ZERO : annualFinance.get(annualFinance.size() - 1).get("profit"));
        result.put("staffCostDetails", staffCostDetails());
        result.put("sourceRevenue", sourceRevenue(todayStart, tomorrowStart));
        result.put("categoryRevenue", categoryRevenue(todayStart, tomorrowStart));
        result.put("dailyRevenueTrend", dailyRevenueTrend(today));
        result.put("costBreakdown", costBreakdown(currentMonth));
        result.put("monthlyFinance", monthlyFinance);
        result.put("annualFinance", annualFinance);
        result.put("lowStock", lowStock());
        result.put("lowStockProducts", lowStock().stream().map(item -> item.get("name")).toList());
        result.put("topProducts", topProducts(todayStart, tomorrowStart));
        result.put("hourlyOrders", hourlyOrders(todayStart));
        return result;
    }

    private BigDecimal revenueBetween(LocalDateTime start, LocalDateTime end) {
        return decimal("""
                SELECT COALESCE(SUM(total_amount), 0)
                FROM orders
                WHERE create_time >= ? AND create_time < ? AND status <> 3
                """, ts(start), ts(end));
    }

    private int countOrdersBetween(LocalDateTime start, LocalDateTime end) {
        return intValue("""
                SELECT COUNT(*)
                FROM orders
                WHERE create_time >= ? AND create_time < ? AND status <> 3
                """, ts(start), ts(end));
    }

    private BigDecimal foodCostBetween(LocalDateTime start, LocalDateTime end) {
        return decimal("""
                SELECT COALESCE(SUM(p.cost_price * oi.quantity), 0)
                FROM orders o
                JOIN order_item oi ON oi.order_id = o.id
                JOIN product p ON p.id = oi.product_id
                WHERE o.create_time >= ? AND o.create_time < ? AND o.status <> 3
                """, ts(start), ts(end));
    }

    private BigDecimal laborCostMonth() {
        return decimal("""
                SELECT COALESCE(SUM(
                  CASE WHEN salary_type = '时薪'
                    THEN hourly_wage * work_hours_this_month
                    ELSE monthly_salary
                  END
                ), 0)
                FROM staff
                WHERE status = 1
                """);
    }

    private Map<String, Object> currentMonthFinance() {
        String currentMonth = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM"));
        List<Map<String, Object>> rows = jdbcTemplate.queryForList("""
                SELECT record_month AS month,
                       dine_in_revenue + miniapp_revenue + ai_order_revenue + delivery_revenue AS revenue,
                       food_cost AS foodCost,
                       manager_labor_cost + employee_labor_cost + part_time_labor_cost AS laborCost,
                       rent_cost + utilities_cost + platform_fee + equipment_cost AS fixedCost,
                       marketing_cost AS marketingCost,
                       other_cost AS otherCost
                FROM finance_record
                WHERE record_month = ?
                """, currentMonth);
        if (rows.isEmpty()) {
            rows = jdbcTemplate.queryForList("""
                    SELECT record_month AS month,
                           dine_in_revenue + miniapp_revenue + ai_order_revenue + delivery_revenue AS revenue,
                           food_cost AS foodCost,
                           manager_labor_cost + employee_labor_cost + part_time_labor_cost AS laborCost,
                           rent_cost + utilities_cost + platform_fee + equipment_cost AS fixedCost,
                           marketing_cost AS marketingCost,
                           other_cost AS otherCost
                    FROM finance_record
                    ORDER BY record_month DESC
                    LIMIT 1
                    """);
        }
        return rows.isEmpty() ? emptyFinance(LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM"))) : withExpense(rows.get(0));
    }

    private List<Map<String, Object>> monthlyFinance() {
        return jdbcTemplate.queryForList("""
                        SELECT record_month AS month,
                               dine_in_revenue + miniapp_revenue + ai_order_revenue + delivery_revenue AS revenue,
                               food_cost AS foodCost,
                               manager_labor_cost + employee_labor_cost + part_time_labor_cost AS laborCost,
                               rent_cost + utilities_cost + platform_fee + equipment_cost AS fixedCost,
                               marketing_cost AS marketingCost,
                               other_cost AS otherCost
                        FROM finance_record
                        ORDER BY record_month
                        """)
                .stream()
                .map(this::withExpense)
                .toList();
    }

    private List<Map<String, Object>> annualFinance() {
        return jdbcTemplate.queryForList("""
                        SELECT LEFT(record_month, 4) AS year,
                               SUM(dine_in_revenue + miniapp_revenue + ai_order_revenue + delivery_revenue) AS revenue,
                               SUM(food_cost) AS foodCost,
                               SUM(manager_labor_cost + employee_labor_cost + part_time_labor_cost) AS laborCost,
                               SUM(rent_cost + utilities_cost + platform_fee + equipment_cost) AS fixedCost,
                               SUM(marketing_cost) AS marketingCost,
                               SUM(other_cost) AS otherCost
                        FROM finance_record
                        GROUP BY LEFT(record_month, 4)
                        ORDER BY year
                        """)
                .stream()
                .map(this::withExpense)
                .toList();
    }

    private Map<String, Object> withExpense(Map<String, Object> row) {
        Map<String, Object> item = new LinkedHashMap<>(row);
        BigDecimal revenue = money(item.get("revenue"));
        BigDecimal foodCost = money(item.get("foodCost"));
        BigDecimal laborCost = money(item.get("laborCost"));
        BigDecimal fixedCost = money(item.get("fixedCost"));
        BigDecimal marketingCost = money(item.get("marketingCost"));
        BigDecimal otherCost = money(item.get("otherCost"));
        BigDecimal expense = money(foodCost.add(laborCost).add(fixedCost).add(marketingCost).add(otherCost));
        item.put("revenue", revenue);
        item.put("foodCost", foodCost);
        item.put("laborCost", laborCost);
        item.put("fixedCost", fixedCost);
        item.put("marketingCost", marketingCost);
        item.put("otherCost", otherCost);
        item.put("expense", expense);
        item.put("profit", money(revenue.subtract(expense)));
        return item;
    }

    private Map<String, Object> emptyFinance(String month) {
        Map<String, Object> item = new LinkedHashMap<>();
        item.put("month", month);
        item.put("revenue", ZERO);
        item.put("foodCost", ZERO);
        item.put("laborCost", ZERO);
        item.put("fixedCost", ZERO);
        item.put("marketingCost", ZERO);
        item.put("otherCost", ZERO);
        item.put("expense", ZERO);
        item.put("profit", ZERO);
        return item;
    }

    private List<Map<String, Object>> staffCostDetails() {
        return jdbcTemplate.queryForList("""
                SELECT id, name, role, shift_name AS shift, salary_type AS salaryType,
                       work_hours_this_month AS workHoursThisMonth,
                       CASE WHEN salary_type = '时薪'
                         THEN hourly_wage * work_hours_this_month
                         ELSE monthly_salary
                       END AS monthlyCost,
                       CASE WHEN salary_type = '时薪'
                         THEN hourly_wage * work_hours_this_month / 30
                         ELSE monthly_salary / 30
                       END AS dailyCost
                FROM staff
                WHERE status = 1
                ORDER BY FIELD(role, '店长', '后厨', '前台', '兼职'), id
                """);
    }

    private List<Map<String, Object>> sourceRevenue(LocalDateTime start, LocalDateTime end) {
        Map<Integer, Map<String, Object>> rows = jdbcTemplate.queryForList("""
                        SELECT source, COUNT(*) AS orderCount, COALESCE(SUM(total_amount), 0) AS revenue
                        FROM orders
                        WHERE create_time >= ? AND create_time < ? AND status <> 3
                        GROUP BY source
                        """, ts(start), ts(end))
                .stream()
                .collect(Collectors.toMap(
                        item -> ((Number) item.get("source")).intValue(),
                        item -> item,
                        (a, b) -> a,
                        LinkedHashMap::new
                ));
        List<Map<String, Object>> result = new ArrayList<>();
        result.add(sourceItem("普通点餐", rows.get(0)));
        result.add(sourceItem("AI 点餐", rows.get(1)));
        return result;
    }

    private Map<String, Object> sourceItem(String name, Map<String, Object> row) {
        Map<String, Object> item = new LinkedHashMap<>();
        item.put("name", name);
        item.put("orderCount", row == null ? 0 : ((Number) row.get("orderCount")).intValue());
        item.put("revenue", row == null ? ZERO : money(row.get("revenue")));
        return item;
    }

    private List<Map<String, Object>> categoryRevenue(LocalDateTime start, LocalDateTime end) {
        return jdbcTemplate.queryForList("""
                SELECT COALESCE(p.category, '其他') AS name, COALESCE(SUM(oi.subtotal), 0) AS revenue
                FROM orders o
                JOIN order_item oi ON oi.order_id = o.id
                LEFT JOIN product p ON p.id = oi.product_id
                WHERE o.create_time >= ? AND o.create_time < ? AND o.status <> 3
                GROUP BY COALESCE(p.category, '其他')
                ORDER BY revenue DESC
                """, ts(start), ts(end));
    }

    private List<Map<String, Object>> dailyRevenueTrend(LocalDate today) {
        List<Map<String, Object>> trend = new ArrayList<>();
        for (int index = 6; index >= 0; index--) {
            LocalDate date = today.minusDays(index);
            LocalDateTime start = date.atStartOfDay();
            LocalDateTime end = date.plusDays(1).atStartOfDay();
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("date", date.format(DateTimeFormatter.ofPattern("MM-dd")));
            item.put("revenue", revenueBetween(start, end));
            item.put("orderCount", countOrdersBetween(start, end));
            trend.add(item);
        }
        return trend;
    }

    private List<Map<String, Object>> costBreakdown(Map<String, Object> currentMonth) {
        return List.of(
                namedValue("食材成本", currentMonth.get("foodCost")),
                namedValue("人力成本", currentMonth.get("laborCost")),
                namedValue("房租水电", currentMonth.get("fixedCost")),
                namedValue("营销费用", currentMonth.get("marketingCost")),
                namedValue("其他费用", currentMonth.get("otherCost"))
        );
    }

    private Map<String, Object> namedValue(String name, Object value) {
        Map<String, Object> item = new LinkedHashMap<>();
        item.put("name", name);
        item.put("value", money(value));
        return item;
    }

    private List<Map<String, Object>> lowStock() {
        return jdbcTemplate.queryForList("""
                SELECT name, category, stock, taste_tags AS tasteTags
                FROM product
                WHERE deleted = 0 AND stock <= 10
                ORDER BY stock ASC, id ASC
                """);
    }

    private List<Map<String, Object>> topProducts(LocalDateTime start, LocalDateTime end) {
        return jdbcTemplate.queryForList("""
                SELECT oi.product_name AS name, SUM(oi.quantity) AS quantity
                FROM orders o
                JOIN order_item oi ON oi.order_id = o.id
                WHERE o.create_time >= ? AND o.create_time < ? AND o.status <> 3
                GROUP BY oi.product_name
                ORDER BY quantity DESC
                LIMIT 6
                """, ts(start), ts(end));
    }

    private List<Map<String, Object>> hourlyOrders(LocalDateTime todayStart) {
        List<Map<String, Object>> rows = new ArrayList<>();
        for (int hour : List.of(10, 11, 12, 13, 14, 17, 18, 19, 20)) {
            LocalDateTime start = todayStart.withHour(hour);
            LocalDateTime end = start.plusHours(1);
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("hour", String.format("%02d:00", hour));
            item.put("count", countOrdersBetween(start, end));
            rows.add(item);
        }
        return rows;
    }

    private BigDecimal decimal(String sql, Object... args) {
        BigDecimal value = jdbcTemplate.queryForObject(sql, BigDecimal.class, args);
        return money(value);
    }

    private int intValue(String sql, Object... args) {
        Number value = jdbcTemplate.queryForObject(sql, Number.class, args);
        return value == null ? 0 : value.intValue();
    }

    private int changeRate(BigDecimal current, BigDecimal previous) {
        if (previous == null || previous.compareTo(BigDecimal.ZERO) == 0) {
            return current != null && current.compareTo(BigDecimal.ZERO) > 0 ? 100 : 0;
        }
        return current.subtract(previous)
                .multiply(BigDecimal.valueOf(100))
                .divide(previous, 0, RoundingMode.HALF_UP)
                .intValue();
    }

    private int rate(BigDecimal value, BigDecimal base) {
        if (base == null || base.compareTo(BigDecimal.ZERO) == 0) {
            return 0;
        }
        return value.multiply(BigDecimal.valueOf(100))
                .divide(base, 0, RoundingMode.HALF_UP)
                .intValue();
    }

    private BigDecimal money(Object value) {
        if (value == null) {
            return ZERO;
        }
        if (value instanceof BigDecimal decimal) {
            return decimal.setScale(2, RoundingMode.HALF_UP);
        }
        if (value instanceof Number number) {
            return BigDecimal.valueOf(number.doubleValue()).setScale(2, RoundingMode.HALF_UP);
        }
        return new BigDecimal(String.valueOf(value)).setScale(2, RoundingMode.HALF_UP);
    }

    private Timestamp ts(LocalDateTime dateTime) {
        return Timestamp.valueOf(dateTime);
    }
}
