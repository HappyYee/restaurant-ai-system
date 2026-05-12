package com.example.restaurant.config;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Configuration
@RequiredArgsConstructor
public class DemoDataInitializer {
    private final JdbcTemplate jdbcTemplate;

    @Bean
    @Order(3)
    public CommandLineRunner demoBusinessDataRunner() {
        return args -> {
            seedProducts();
            seedStaff();
            seedUsers();
            seedOrdersWhenNeeded();
            seedFinanceRecords();
        };
    }

    private void seedProducts() {
        List<ProductSeed> products = List.of(
                new ProductSeed("番茄鸡蛋饭", "主食", "22.00", "9.00", 42, "清淡,不辣,实惠", "番茄鸡蛋搭配米饭，口味清淡，适合日常午餐。", 8),
                new ProductSeed("牛肉饭", "主食", "28.00", "13.00", 5, "微辣,热销,饱腹", "牛肉搭配米饭，微辣口味，门店热销菜品。", 10),
                new ProductSeed("鸡腿饭", "主食", "26.00", "12.00", 36, "咸香,饱腹,热销", "香煎鸡腿搭配米饭，适合想吃饱的顾客。", 12),
                new ProductSeed("鸡胸肉轻食饭", "主食", "30.00", "14.00", 22, "清淡,高蛋白,健康", "鸡胸肉搭配蔬菜和米饭，适合清淡健康需求。", 10),
                new ProductSeed("柠檬茶", "饮品", "8.00", "2.40", 88, "清爽,甜,饮品", "清爽柠檬茶，适合搭配主食。", 2),
                new ProductSeed("无糖绿茶", "饮品", "6.00", "1.80", 74, "清淡,无糖,饮品", "无糖绿茶，适合清淡低糖需求。", 1),
                new ProductSeed("香辣鸡翅", "小吃", "16.00", "7.00", 8, "香辣,小吃", "香辣口味鸡翅，适合加餐。", 8),
                new ProductSeed("薯条", "小吃", "10.00", "3.50", 31, "小吃,实惠", "经典薯条，可作为加餐。", 5)
        );
        for (ProductSeed product : products) {
            Long count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM product WHERE name = ?", Long.class, product.name());
            if (count != null && count > 0) {
                jdbcTemplate.update("""
                                UPDATE product
                                SET category = ?, price = ?, cost_price = ?, stock = ?, status = 1,
                                    taste_tags = ?, description = ?, cook_time = ?, deleted = 0
                                WHERE name = ?
                                """,
                        product.category(), product.price(), product.costPrice(), product.stock(), product.tags(),
                        product.description(), product.cookTime(), product.name());
            } else {
                jdbcTemplate.update("""
                                INSERT INTO product
                                (name, category, price, cost_price, stock, status, taste_tags, description, image_url, cook_time)
                                VALUES (?, ?, ?, ?, ?, 1, ?, ?, '', ?)
                                """,
                        product.name(), product.category(), product.price(), product.costPrice(), product.stock(),
                        product.tags(), product.description(), product.cookTime());
            }
        }
    }

    private void seedStaff() {
        List<StaffSeed> staff = List.of(
                new StaffSeed("李敏", "13800010001", "店长", "全日班", "月薪", "7200.00", "0.00", "176.0", "2024-03-12", "负责门店排班、库存和日结。"),
                new StaffSeed("王磊", "13800010002", "后厨", "早班", "月薪", "5600.00", "0.00", "168.0", "2024-06-01", "主食出餐。"),
                new StaffSeed("陈佳", "13800010003", "前台", "午晚班", "月薪", "4800.00", "0.00", "160.0", "2025-02-18", "收银、打包和小程序订单处理。"),
                new StaffSeed("赵阳", "13800010004", "兼职", "晚班", "时薪", "0.00", "28.00", "72.0", "2025-09-03", "晚高峰支援。"),
                new StaffSeed("周宁", "13800010005", "后厨", "晚班", "月薪", "5200.00", "0.00", "152.0", "2025-10-20", "晚餐出餐与备货。")
        );
        for (StaffSeed item : staff) {
            Long count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM staff WHERE phone = ?", Long.class, item.phone());
            if (count != null && count > 0) {
                jdbcTemplate.update("""
                                UPDATE staff
                                SET name = ?, role = ?, shift_name = ?, salary_type = ?, monthly_salary = ?,
                                    hourly_wage = ?, work_hours_this_month = ?, status = 1, hire_date = ?, remark = ?
                                WHERE phone = ?
                                """,
                        item.name(), item.role(), item.shift(), item.salaryType(), item.monthlySalary(),
                        item.hourlyWage(), item.hours(), item.hireDate(), item.remark(), item.phone());
            } else {
                jdbcTemplate.update("""
                                INSERT INTO staff
                                (name, phone, role, shift_name, salary_type, monthly_salary, hourly_wage,
                                 work_hours_this_month, status, hire_date, remark)
                                VALUES (?, ?, ?, ?, ?, ?, ?, ?, 1, ?, ?)
                                """,
                        item.name(), item.phone(), item.role(), item.shift(), item.salaryType(), item.monthlySalary(),
                        item.hourlyWage(), item.hours(), item.hireDate(), item.remark());
            }
        }
    }

    private void seedUsers() {
        List<String> names = List.of(
                "林小满", "陈一禾", "周嘉怡", "王俊凯", "何予安", "刘思源", "宋雨晴", "张晨",
                "杨可", "赵明轩", "吴念", "许知夏", "孙若宁", "郭远", "叶星河", "唐小橙",
                "邓佳", "罗一帆", "韩清", "马宁", "朱可心", "高以辰", "魏南", "顾安"
        );
        for (int index = 0; index < names.size(); index++) {
            String openid = String.format("demo_member_%03d", index + 1);
            LocalDateTime memberSince = LocalDate.now().minusDays(110L - index * 3L).atTime(10, 0);
            jdbcTemplate.update("""
                            INSERT INTO `user`
                            (openid, nickname, avatar_url, member_level, points, total_spent, member_since, create_time, update_time)
                            VALUES (?, ?, '', '普通会员', 0, 0.00, ?, ?, ?)
                            ON DUPLICATE KEY UPDATE nickname = VALUES(nickname), member_since = VALUES(member_since)
                            """,
                    openid, names.get(index), Timestamp.valueOf(memberSince), Timestamp.valueOf(memberSince),
                    Timestamp.valueOf(LocalDateTime.now()));
        }
    }

    private void seedOrdersWhenNeeded() {
        Long todayCount = jdbcTemplate.queryForObject("""
                SELECT COUNT(*) FROM orders
                WHERE order_no LIKE 'DM%' AND create_time >= CURDATE()
                """, Long.class);
        if (todayCount != null && todayCount >= 60) {
            return;
        }

        jdbcTemplate.update("""
                DELETE FROM order_item
                WHERE order_id IN (SELECT id FROM orders WHERE order_no LIKE 'DM%')
                """);
        jdbcTemplate.update("DELETE FROM orders WHERE order_no LIKE 'DM%'");
        jdbcTemplate.update("UPDATE `user` SET total_spent = 0.00, points = 0, member_level = '普通会员' WHERE openid LIKE 'demo_member_%'");

        List<DemoUser> users = jdbcTemplate.query("""
                        SELECT id, openid FROM `user`
                        WHERE openid LIKE 'demo_member_%'
                        ORDER BY openid
                        """,
                (rs, rowNum) -> new DemoUser(rs.getLong("id"), rs.getString("openid")));
        Map<String, DemoProduct> products = loadProducts();
        String[] mains = {"番茄鸡蛋饭", "牛肉饭", "鸡腿饭", "鸡胸肉轻食饭"};
        String[] drinks = {"柠檬茶", "无糖绿茶"};
        String[] snacks = {"香辣鸡翅", "薯条"};
        Map<String, BigDecimal> spentMap = new LinkedHashMap<>();
        Map<String, Integer> pointMap = new LinkedHashMap<>();
        users.forEach(user -> {
            spentMap.put(user.openid(), BigDecimal.ZERO);
            pointMap.put(user.openid(), 0);
        });

        int sequence = 1;
        for (int daysAgo = 20; daysAgo >= 0; daysAgo--) {
            LocalDate date = LocalDate.now().minusDays(daysAgo);
            int orderCount = daysAgo == 0 ? 88 : daysAgo == 1 ? 76 : 38 + (daysAgo % 9);
            for (int index = 0; index < orderCount; index++) {
                DemoUser user = users.get((index + daysAgo * 2) % users.size());
                List<OrderLine> lines = new ArrayList<>();
                DemoProduct main = products.get(mains[(index + daysAgo) % mains.length]);
                lines.add(new OrderLine(main, 1));
                if ((index + daysAgo) % 3 != 0) {
                    lines.add(new OrderLine(products.get(drinks[(index + daysAgo) % drinks.length]), 1));
                }
                if ((index + daysAgo) % 4 == 0) {
                    lines.add(new OrderLine(products.get(snacks[(index / 2 + daysAgo) % snacks.length]), 1));
                }
                if ((index + daysAgo) % 9 == 0) {
                    lines.add(new OrderLine(products.get("薯条"), 1));
                }

                BigDecimal originalAmount = lines.stream()
                        .map(line -> memberPrice(line.product()).multiply(BigDecimal.valueOf(line.quantity())))
                        .reduce(BigDecimal.ZERO, BigDecimal::add)
                        .setScale(2, RoundingMode.HALF_UP);
                int availablePoints = pointMap.get(user.openid());
                int pointsUsed = (index + daysAgo) % 6 == 0 ? calculatePointsUsed(availablePoints, originalAmount) : 0;
                BigDecimal pointsDiscount = BigDecimal.valueOf(pointsUsed).multiply(new BigDecimal("0.01")).setScale(2, RoundingMode.HALF_UP);
                BigDecimal paidAmount = originalAmount.subtract(pointsDiscount).setScale(2, RoundingMode.HALF_UP);
                String currentLevel = resolveLevel(spentMap.get(user.openid()));
                int pointsEarned = calculateEarnedPoints(currentLevel, paidAmount);
                int source = (index + daysAgo) % 4 == 0 ? 1 : 0;
                int status = daysAgo == 0 ? (index % 6 == 0 ? 0 : index % 6 == 1 ? 1 : 2) : 2;
                LocalDateTime createdAt = date.atTime(10 + (index % 10), (index * 11 + daysAgo) % 60);
                String orderNo = "DM" + date.format(DateTimeFormatter.BASIC_ISO_DATE) + String.format("%03d", sequence++);

                Long orderId = insertOrder(orderNo, user.id(), originalAmount, paidAmount, pointsUsed, pointsDiscount,
                        pointsEarned, status, source, createdAt, source == 1 ? "AI推荐组合" : "门店小程序下单");
                for (OrderLine line : lines) {
                    BigDecimal unitPrice = memberPrice(line.product());
                    jdbcTemplate.update("""
                                    INSERT INTO order_item
                                    (order_id, product_id, product_name, quantity, unit_price, subtotal, create_time)
                                    VALUES (?, ?, ?, ?, ?, ?, ?)
                                    """,
                            orderId, line.product().id(), line.product().name(), line.quantity(), unitPrice,
                            unitPrice.multiply(BigDecimal.valueOf(line.quantity())).setScale(2, RoundingMode.HALF_UP),
                            Timestamp.valueOf(createdAt));
                }

                spentMap.put(user.openid(), spentMap.get(user.openid()).add(paidAmount));
                pointMap.put(user.openid(), Math.max(0, availablePoints - pointsUsed) + pointsEarned);
            }
        }

        for (DemoUser user : users) {
            BigDecimal spent = spentMap.get(user.openid()).setScale(2, RoundingMode.HALF_UP);
            jdbcTemplate.update("""
                            UPDATE `user`
                            SET total_spent = ?, points = ?, member_level = ?, update_time = NOW()
                            WHERE id = ?
                            """,
                    spent, pointMap.get(user.openid()), resolveLevel(spent), user.id());
        }
    }

    private void seedFinanceRecords() {
        for (int offset = 11; offset >= 0; offset--) {
            LocalDate month = LocalDate.now().minusMonths(offset).withDayOfMonth(1);
            int index = 11 - offset;
            BigDecimal revenue = new BigDecimal(52000 + index * 2100 + (index % 3) * 2600);
            if (offset == 0) {
                revenue = new BigDecimal("36800");
            }
            BigDecimal dineIn = revenue.multiply(new BigDecimal("0.42")).setScale(2, RoundingMode.HALF_UP);
            BigDecimal miniapp = revenue.multiply(new BigDecimal("0.33")).setScale(2, RoundingMode.HALF_UP);
            BigDecimal ai = revenue.multiply(new BigDecimal("0.15")).setScale(2, RoundingMode.HALF_UP);
            BigDecimal delivery = revenue.subtract(dineIn).subtract(miniapp).subtract(ai).setScale(2, RoundingMode.HALF_UP);
            BigDecimal food = revenue.multiply(new BigDecimal("0.36")).setScale(2, RoundingMode.HALF_UP);
            BigDecimal manager = new BigDecimal("7200.00");
            BigDecimal employee = new BigDecimal("15600.00");
            BigDecimal partTime = new BigDecimal(1800 + index * 70).setScale(2, RoundingMode.HALF_UP);
            BigDecimal rent = new BigDecimal("8200.00");
            BigDecimal utilities = new BigDecimal(2600 + (index % 4) * 120).setScale(2, RoundingMode.HALF_UP);
            BigDecimal marketing = new BigDecimal(2800 + index * 95).setScale(2, RoundingMode.HALF_UP);
            BigDecimal platform = revenue.multiply(new BigDecimal("0.035")).setScale(2, RoundingMode.HALF_UP);
            BigDecimal equipment = new BigDecimal("950.00");
            BigDecimal other = new BigDecimal("1200.00");
            jdbcTemplate.update("""
                            INSERT INTO finance_record
                            (record_month, dine_in_revenue, miniapp_revenue, ai_order_revenue, delivery_revenue,
                             food_cost, manager_labor_cost, employee_labor_cost, part_time_labor_cost, rent_cost,
                             utilities_cost, marketing_cost, platform_fee, equipment_cost, other_cost, remark)
                            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                            ON DUPLICATE KEY UPDATE
                              dine_in_revenue = VALUES(dine_in_revenue),
                              miniapp_revenue = VALUES(miniapp_revenue),
                              ai_order_revenue = VALUES(ai_order_revenue),
                              delivery_revenue = VALUES(delivery_revenue),
                              food_cost = VALUES(food_cost),
                              manager_labor_cost = VALUES(manager_labor_cost),
                              employee_labor_cost = VALUES(employee_labor_cost),
                              part_time_labor_cost = VALUES(part_time_labor_cost),
                              rent_cost = VALUES(rent_cost),
                              utilities_cost = VALUES(utilities_cost),
                              marketing_cost = VALUES(marketing_cost),
                              platform_fee = VALUES(platform_fee),
                              equipment_cost = VALUES(equipment_cost),
                              other_cost = VALUES(other_cost),
                              remark = VALUES(remark)
                            """,
                    month.format(DateTimeFormatter.ofPattern("yyyy-MM")),
                    dineIn, miniapp, ai, delivery, food, manager, employee, partTime, rent, utilities,
                    marketing, platform, equipment, other, "演示月度财务数据");
        }
    }

    private Map<String, DemoProduct> loadProducts() {
        List<DemoProduct> rows = jdbcTemplate.query("""
                        SELECT id, name, category, price, cost_price
                        FROM product
                        WHERE deleted = 0 AND status = 1
                        """,
                (rs, rowNum) -> new DemoProduct(
                        rs.getLong("id"),
                        rs.getString("name"),
                        rs.getString("category"),
                        rs.getBigDecimal("price"),
                        rs.getBigDecimal("cost_price")));
        return rows.stream().collect(LinkedHashMap::new, (map, item) -> map.put(item.name(), item), LinkedHashMap::putAll);
    }

    private Long insertOrder(String orderNo, Long userId, BigDecimal originalAmount, BigDecimal paidAmount,
                             int pointsUsed, BigDecimal pointsDiscount, int pointsEarned, int status,
                             int source, LocalDateTime createdAt, String remark) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement statement = connection.prepareStatement("""
                    INSERT INTO orders
                    (order_no, user_id, original_amount, total_amount, points_used, points_discount, points_earned,
                     status, remark, source, create_time, update_time)
                    VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                    """, Statement.RETURN_GENERATED_KEYS);
            statement.setString(1, orderNo);
            statement.setLong(2, userId);
            statement.setBigDecimal(3, originalAmount);
            statement.setBigDecimal(4, paidAmount);
            statement.setInt(5, pointsUsed);
            statement.setBigDecimal(6, pointsDiscount);
            statement.setInt(7, pointsEarned);
            statement.setInt(8, status);
            statement.setString(9, remark);
            statement.setInt(10, source);
            statement.setTimestamp(11, Timestamp.valueOf(createdAt));
            statement.setTimestamp(12, Timestamp.valueOf(createdAt));
            return statement;
        }, keyHolder);
        return keyHolder.getKey().longValue();
    }

    private BigDecimal memberPrice(DemoProduct product) {
        BigDecimal discount = "饮品".equals(product.category()) ? new BigDecimal("0.85") : new BigDecimal("0.90");
        return product.price().multiply(discount).setScale(2, RoundingMode.HALF_UP);
    }

    private int calculatePointsUsed(int availablePoints, BigDecimal originalAmount) {
        int normalized = availablePoints / 50 * 50;
        int orderCap = originalAmount.multiply(new BigDecimal("0.10"))
                .divide(new BigDecimal("0.01"), 0, RoundingMode.DOWN)
                .intValue() / 50 * 50;
        return Math.min(Math.min(normalized, 500), orderCap);
    }

    private int calculateEarnedPoints(String memberLevel, BigDecimal paidAmount) {
        BigDecimal multiplier = "金卡会员".equals(memberLevel)
                ? new BigDecimal("1.50")
                : "银卡会员".equals(memberLevel) ? new BigDecimal("1.20") : BigDecimal.ONE;
        return paidAmount.multiply(multiplier).setScale(0, RoundingMode.DOWN).intValue();
    }

    private String resolveLevel(BigDecimal spent) {
        if (spent.compareTo(new BigDecimal("300")) >= 0) {
            return "金卡会员";
        }
        if (spent.compareTo(new BigDecimal("100")) >= 0) {
            return "银卡会员";
        }
        return "普通会员";
    }

    private record ProductSeed(String name, String category, String price, String costPrice, int stock,
                               String tags, String description, int cookTime) {
    }

    private record StaffSeed(String name, String phone, String role, String shift, String salaryType,
                             String monthlySalary, String hourlyWage, String hours, String hireDate, String remark) {
    }

    private record DemoUser(Long id, String openid) {
    }

    private record DemoProduct(Long id, String name, String category, BigDecimal price, BigDecimal costPrice) {
    }

    private record OrderLine(DemoProduct product, int quantity) {
    }
}
