package com.example.restaurant.controller;

import com.example.restaurant.common.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/staff")
public class AdminStaffController {
    private final JdbcTemplate jdbcTemplate;

    @GetMapping
    public Result<List<Map<String, Object>>> list(@RequestParam(required = false) String keyword,
                                                  @RequestParam(required = false) String role,
                                                  @RequestParam(required = false) Integer status) {
        List<Object> args = new ArrayList<>();
        StringBuilder sql = new StringBuilder("""
                SELECT id, name, phone, role, shift_name AS shift, salary_type AS salaryType,
                       monthly_salary AS monthlySalary, hourly_wage AS hourlyWage,
                       work_hours_this_month AS workHoursThisMonth, status,
                       DATE_FORMAT(hire_date, '%Y-%m-%d') AS hireDate, remark
                FROM staff
                WHERE 1 = 1
                """);
        if (keyword != null && !keyword.isBlank()) {
            sql.append(" AND (name LIKE ? OR phone LIKE ? OR role LIKE ? OR shift_name LIKE ?)");
            String value = "%" + keyword + "%";
            args.add(value);
            args.add(value);
            args.add(value);
            args.add(value);
        }
        if (role != null && !role.isBlank()) {
            sql.append(" AND role = ?");
            args.add(role);
        }
        if (status != null) {
            sql.append(" AND status = ?");
            args.add(status);
        }
        sql.append(" ORDER BY status DESC, id DESC");
        return Result.success(jdbcTemplate.queryForList(sql.toString(), args.toArray()));
    }

    @PostMapping
    public Result<Map<String, Object>> create(@RequestBody Map<String, Object> payload) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement statement = connection.prepareStatement("""
                    INSERT INTO staff
                    (name, phone, role, shift_name, salary_type, monthly_salary, hourly_wage,
                     work_hours_this_month, status, hire_date, remark)
                    VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                    """, Statement.RETURN_GENERATED_KEYS);
            fillStatement(statement, payload);
            return statement;
        }, keyHolder);
        Number id = keyHolder.getKey();
        return Result.success(findById(id == null ? null : id.longValue()));
    }

    @PutMapping("/{id}")
    public Result<Map<String, Object>> update(@PathVariable Long id, @RequestBody Map<String, Object> payload) {
        jdbcTemplate.update("""
                UPDATE staff
                SET name = ?, phone = ?, role = ?, shift_name = ?, salary_type = ?,
                    monthly_salary = ?, hourly_wage = ?, work_hours_this_month = ?,
                    status = ?, hire_date = ?, remark = ?
                WHERE id = ?
                """,
                text(payload, "name"), text(payload, "phone"), text(payload, "role"), text(payload, "shift"),
                text(payload, "salaryType"), decimal(payload, "monthlySalary"), decimal(payload, "hourlyWage"),
                decimal(payload, "workHoursThisMonth"), integer(payload, "status", 1), date(payload, "hireDate"),
                text(payload, "remark"), id);
        return Result.success(findById(id));
    }

    @PatchMapping("/{id}/status")
    public Result<Void> updateStatus(@PathVariable Long id, @RequestParam Integer status) {
        jdbcTemplate.update("UPDATE staff SET status = ? WHERE id = ?", status, id);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        jdbcTemplate.update("DELETE FROM staff WHERE id = ?", id);
        return Result.success();
    }

    private Map<String, Object> findById(Long id) {
        if (id == null) {
            return Map.of();
        }
        return jdbcTemplate.queryForMap("""
                SELECT id, name, phone, role, shift_name AS shift, salary_type AS salaryType,
                       monthly_salary AS monthlySalary, hourly_wage AS hourlyWage,
                       work_hours_this_month AS workHoursThisMonth, status,
                       DATE_FORMAT(hire_date, '%Y-%m-%d') AS hireDate, remark
                FROM staff
                WHERE id = ?
                """, id);
    }

    private void fillStatement(PreparedStatement statement, Map<String, Object> payload) throws java.sql.SQLException {
        statement.setString(1, text(payload, "name"));
        statement.setString(2, text(payload, "phone"));
        statement.setString(3, text(payload, "role"));
        statement.setString(4, text(payload, "shift"));
        statement.setString(5, text(payload, "salaryType"));
        statement.setBigDecimal(6, decimal(payload, "monthlySalary"));
        statement.setBigDecimal(7, decimal(payload, "hourlyWage"));
        statement.setBigDecimal(8, decimal(payload, "workHoursThisMonth"));
        statement.setInt(9, integer(payload, "status", 1));
        statement.setDate(10, date(payload, "hireDate"));
        statement.setString(11, text(payload, "remark"));
    }

    private String text(Map<String, Object> payload, String key) {
        Object value = payload.get(key);
        return value == null ? "" : String.valueOf(value);
    }

    private Integer integer(Map<String, Object> payload, String key, int fallback) {
        Object value = payload.get(key);
        return value == null || String.valueOf(value).isBlank() ? fallback : Integer.valueOf(String.valueOf(value));
    }

    private BigDecimal decimal(Map<String, Object> payload, String key) {
        Object value = payload.get(key);
        return value == null || String.valueOf(value).isBlank() ? BigDecimal.ZERO : new BigDecimal(String.valueOf(value));
    }

    private Date date(Map<String, Object> payload, String key) {
        Object value = payload.get(key);
        return value == null || String.valueOf(value).isBlank() ? null : Date.valueOf(String.valueOf(value));
    }
}
