package com.example.restaurant.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.restaurant.client.DeepSeekClient;
import com.example.restaurant.dto.AiChatRequest;
import com.example.restaurant.dto.AiOrderRequest;
import com.example.restaurant.entity.AiChatLog;
import com.example.restaurant.entity.OrderItem;
import com.example.restaurant.entity.Orders;
import com.example.restaurant.entity.Product;
import com.example.restaurant.entity.User;
import com.example.restaurant.mapper.AiChatLogMapper;
import com.example.restaurant.mapper.OrderItemMapper;
import com.example.restaurant.mapper.OrdersMapper;
import com.example.restaurant.mapper.ProductMapper;
import com.example.restaurant.mapper.UserMapper;
import com.example.restaurant.service.AiService;
import com.example.restaurant.service.BusinessStatsService;
import com.example.restaurant.service.MemberService;
import com.example.restaurant.vo.AiChatResponseVO;
import com.example.restaurant.vo.AiOrderRecommendationVO;
import com.example.restaurant.vo.MemberProfileVO;
import com.example.restaurant.vo.ProductVO;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AiServiceImpl implements AiService {
    private static final String BUSINESS_SCENE = "business_analysis";
    private static final String ORDER_SCENE = "order_recommend";

    private final DeepSeekClient deepSeekClient;
    private final ObjectMapper objectMapper;
    private final ProductMapper productMapper;
    private final UserMapper userMapper;
    private final OrdersMapper ordersMapper;
    private final OrderItemMapper orderItemMapper;
    private final AiChatLogMapper aiChatLogMapper;
    private final BusinessStatsService businessStatsService;
    private final MemberService memberService;

    @Override
    public AiChatResponseVO businessChat(Long adminId, AiChatRequest request) {
        String sessionId = resolveSessionId(request.getSessionId());
        log(sessionId, null, "user", request.getMessage(), BUSINESS_SCENE);

        AiChatResponseVO response;
        try {
            String content = deepSeekClient.chat(List.of(
                    Map.of("role", "system", "content", businessSystemPrompt()),
                    Map.of("role", "user", "content", buildBusinessUserPrompt(request.getMessage()))
            ));
            response = parseBusinessResponse(sessionId, content);
            response.setProvider("DeepSeek");
            response.setModel(deepSeekClient.model());
            response.setFallback(false);
        } catch (Exception ex) {
            response = fallbackBusinessResponse(sessionId, request.getMessage(), ex.getMessage());
        }

        log(sessionId, null, "assistant", response.getAnswer(), BUSINESS_SCENE);
        return response;
    }

    @Override
    public AiOrderRecommendationVO recommendOrder(Long userId, AiOrderRequest request) {
        String sessionId = resolveSessionId(request.getSessionId());
        User user = userMapper.selectById(userId);
        List<Product> products = availableProducts();
        log(sessionId, userId, "user", request.getMessage(), ORDER_SCENE);

        AiOrderRecommendationVO response;
        try {
            String content = deepSeekClient.chat(List.of(
                    Map.of("role", "system", "content", orderSystemPrompt()),
                    Map.of("role", "user", "content", buildOrderUserPrompt(user, products, request.getMessage()))
            ));
            response = parseOrderResponse(sessionId, content, products);
            if (response.getPlans().isEmpty()) {
                response = fallbackOrderResponse(sessionId, user, products, request.getMessage(), "DeepSeek 返回的推荐为空");
            } else {
                response.setProvider("DeepSeek");
                response.setModel(deepSeekClient.model());
                response.setFallback(false);
            }
        } catch (Exception ex) {
            response = fallbackOrderResponse(sessionId, user, products, request.getMessage(), ex.getMessage());
        }

        log(sessionId, userId, "assistant", safeWrite(response), ORDER_SCENE);
        return response;
    }

    private String businessSystemPrompt() {
        return """
                你是餐饮门店经营分析助手。你只能基于用户提供的门店数据回答，不能编造数据库中没有的具体数字。
                输出必须是 JSON，不要使用 Markdown。格式：
                {"thinking":["可展示给用户的分析依据1","分析依据2"],"answer":"直接回答店长的问题","actions":["可执行建议1","可执行建议2"]}
                thinking 字段是给用户看的简短分析依据，不输出模型内部推理过程。
                """;
    }

    private String orderSystemPrompt() {
        return """
                你是餐饮门店小程序里的 AI 点餐助手。你要结合会员历史、预算、口味、出餐时间和当前库存生成餐品组合。
                只能推荐菜单中存在且库存大于 0 的 productId。价格必须使用菜单给出的 memberPrice。
                输出必须是 JSON，不要使用 Markdown。格式：
                {"thinking":["可展示给用户的分析依据1","分析依据2"],"plans":[{"name":"方案名","reason":"推荐原因","items":[{"productId":1,"quantity":1}]}]}
                thinking 字段是给顾客看的服务过程，不输出模型内部推理过程。
                """;
    }

    private String buildBusinessUserPrompt(String question) {
        Map<String, Object> dashboard = businessStatsService.dashboard();
        Map<String, Object> memberStats = memberService.getMemberStats();
        List<Product> lowStock = productMapper.selectList(new LambdaQueryWrapper<Product>()
                .le(Product::getStock, 10)
                .orderByAsc(Product::getStock)
                .last("LIMIT 8"));
        List<Product> products = productMapper.selectList(new LambdaQueryWrapper<Product>()
                .eq(Product::getStatus, 1)
                .orderByAsc(Product::getCategory)
                .last("LIMIT 20"));

        Map<String, Object> context = new LinkedHashMap<>();
        context.put("dashboard", dashboard);
        context.put("memberStats", memberStats);
        context.put("lowStockProducts", lowStock.stream().map(this::productBrief).toList());
        context.put("availableProducts", products.stream().map(this::productBrief).toList());
        return "门店数据上下文：" + safeWrite(context) + "\n店长问题：" + question;
    }

    private String buildOrderUserPrompt(User user, List<Product> products, String message) {
        Map<String, Object> context = new LinkedHashMap<>();
        context.put("member", user == null ? Map.of() : Map.of(
                "userId", user.getId(),
                "memberLevel", user.getMemberLevel() == null ? "普通会员" : user.getMemberLevel(),
                "points", user.getPoints() == null ? 0 : user.getPoints(),
                "totalSpent", user.getTotalSpent() == null ? BigDecimal.ZERO : user.getTotalSpent()
        ));
        context.put("recentOrders", recentOrderBrief(user == null ? null : user.getId()));
        context.put("menu", products.stream().map(this::productBrief).toList());
        return "顾客点餐需求：" + message + "\n可用上下文：" + safeWrite(context);
    }

    private Map<String, Object> productBrief(Product product) {
        Map<String, Object> item = new LinkedHashMap<>();
        item.put("productId", product.getId());
        item.put("name", product.getName());
        item.put("category", product.getCategory());
        item.put("price", product.getPrice());
        item.put("memberPrice", ProductVO.calcMemberPrice(product));
        item.put("stock", product.getStock());
        item.put("tasteTags", product.getTasteTags());
        item.put("cookTime", product.getCookTime());
        return item;
    }

    private List<Map<String, Object>> recentOrderBrief(Long userId) {
        if (userId == null) {
            return List.of();
        }
        List<Orders> orders = ordersMapper.selectList(new LambdaQueryWrapper<Orders>()
                .eq(Orders::getUserId, userId)
                .ne(Orders::getStatus, 3)
                .orderByDesc(Orders::getCreateTime)
                .last("LIMIT 5"));
        return orders.stream().map(order -> {
            List<OrderItem> items = orderItemMapper.selectList(new LambdaQueryWrapper<OrderItem>()
                    .eq(OrderItem::getOrderId, order.getId()));
            Map<String, Object> brief = new LinkedHashMap<>();
            brief.put("orderNo", order.getOrderNo());
            brief.put("totalAmount", order.getTotalAmount());
            brief.put("items", items.stream()
                    .map(item -> item.getProductName() + "x" + item.getQuantity())
                    .toList());
            return brief;
        }).toList();
    }

    private AiChatResponseVO parseBusinessResponse(String sessionId, String content) {
        try {
            Map<String, Object> map = objectMapper.readValue(extractJson(content), new TypeReference<>() {});
            AiChatResponseVO response = new AiChatResponseVO();
            response.setSessionId(sessionId);
            response.setThinking(asStringList(map.get("thinking")));
            response.setAnswer(String.valueOf(map.getOrDefault("answer", content)));
            response.setActions(asStringList(map.get("actions")));
            return response;
        } catch (Exception ex) {
            AiChatResponseVO response = new AiChatResponseVO();
            response.setSessionId(sessionId);
            response.setThinking(List.of("已读取门店经营、会员和菜品数据", "模型返回了文本内容，系统已转为可读报告"));
            response.setAnswer(content);
            response.setActions(List.of("继续追问具体月份、菜品或会员群体", "结合财务看板核对成本结构"));
            return response;
        }
    }

    private AiOrderRecommendationVO parseOrderResponse(String sessionId, String content, List<Product> products) {
        Map<Long, Product> productMap = products.stream().collect(Collectors.toMap(Product::getId, item -> item));
        try {
            Map<String, Object> map = objectMapper.readValue(extractJson(content), new TypeReference<>() {});
            AiOrderRecommendationVO response = new AiOrderRecommendationVO();
            response.setSessionId(sessionId);
            response.setThinking(asStringList(map.get("thinking")));
            Object plansValue = map.get("plans") == null ? List.of() : map.get("plans");
            List<AiOrderRecommendationVO.Plan> plans = objectMapper.convertValue(
                    plansValue,
                    new TypeReference<List<AiOrderRecommendationVO.Plan>>() {}
            );
            response.setPlans(plans.stream()
                    .map(plan -> normalizePlan(plan, productMap))
                    .filter(plan -> !plan.getItems().isEmpty())
                    .limit(3)
                    .toList());
            return response;
        } catch (Exception ex) {
            AiOrderRecommendationVO response = new AiOrderRecommendationVO();
            response.setSessionId(sessionId);
            return response;
        }
    }

    private AiOrderRecommendationVO fallbackOrderResponse(String sessionId, User user, List<Product> products,
                                                          String message, String reason) {
        AiOrderRecommendationVO response = new AiOrderRecommendationVO();
        response.setSessionId(sessionId);
        response.setProvider("local-rule");
        response.setModel("fallback");
        response.setFallback(true);
        response.setErrorMessage(reason);
        MemberProfileVO profile = user == null ? null : MemberProfileVO.from(user);
        response.setThinking(new ArrayList<>(List.of(
                "已读取当前菜单、库存和会员价",
                profile == null ? "未读取到会员档案，按普通偏好生成" : "已结合 " + profile.getMemberLevel() + " 的历史消费信息",
                "按预算、口味关键词和出餐时间生成组合"
        )));

        List<Product> sorted = scoreProducts(products, message);
        BigDecimal budget = extractBudget(message);
        AiOrderRecommendationVO.Plan first = composePlan("会员优选方案", "按你的需求优先匹配口味、预算和会员价。", sorted, budget);
        AiOrderRecommendationVO.Plan second = composePlan("快速出餐方案", "优先选择出餐时间更短、库存稳定的餐品。",
                sorted.stream().sorted(Comparator.comparing(product -> product.getCookTime() == null ? 99 : product.getCookTime())).toList(),
                budget);
        response.setPlans(List.of(first, second).stream().filter(plan -> !plan.getItems().isEmpty()).toList());
        return response;
    }

    private AiChatResponseVO fallbackBusinessResponse(String sessionId, String question, String reason) {
        Map<String, Object> dashboard = businessStatsService.dashboard();
        Map<String, Object> memberStats = memberService.getMemberStats();
        AiChatResponseVO response = new AiChatResponseVO();
        response.setSessionId(sessionId);
        response.setProvider("local-rule");
        response.setModel("fallback");
        response.setFallback(true);
        response.setErrorMessage(reason);
        response.setThinking(List.of("已读取经营看板核心指标", "已读取会员规模、消费和等级分布", "DeepSeek 暂不可用时启用本地经营规则"));
        response.setAnswer("当前门店今日营业额约 " + dashboard.getOrDefault("todayRevenue", 0)
                + " 元，今日订单 " + dashboard.getOrDefault("todayOrderCount", 0)
                + " 单，会员累计消费约 " + memberStats.getOrDefault("totalMemberSpent", 0)
                + " 元。针对“" + question + "”，建议优先从热销菜品组合、会员复购和低库存风险三个方向处理。");
        response.setActions(List.of(
                "把热销主食和饮品打成会员组合，突出会员价",
                "查看低库存菜品，避免 AI 推荐到即将缺货的餐品",
                "追问某个月或某个成本项，获得更细的经营解释"
        ));
        return response;
    }

    private AiOrderRecommendationVO.Plan normalizePlan(AiOrderRecommendationVO.Plan plan, Map<Long, Product> productMap) {
        AiOrderRecommendationVO.Plan normalized = new AiOrderRecommendationVO.Plan();
        normalized.setName(plan.getName() == null || plan.getName().isBlank() ? "AI 推荐方案" : plan.getName());
        normalized.setReason(plan.getReason() == null || plan.getReason().isBlank() ? "根据会员价、库存和需求生成。" : plan.getReason());

        BigDecimal total = BigDecimal.ZERO;
        List<AiOrderRecommendationVO.Item> items = new ArrayList<>();
        List<AiOrderRecommendationVO.Item> planItems = plan.getItems() == null ? List.of() : plan.getItems();
        for (AiOrderRecommendationVO.Item item : planItems) {
            Product product = productMap.get(item.getProductId());
            if (product == null) {
                continue;
            }
            AiOrderRecommendationVO.Item normalizedItem = toPlanItem(product, item.getQuantity() == null ? 1 : item.getQuantity());
            items.add(normalizedItem);
            total = total.add(normalizedItem.getSubtotal());
        }
        normalized.setItems(items);
        normalized.setTotalAmount(total.setScale(2, RoundingMode.HALF_UP));
        return normalized;
    }

    private List<Product> scoreProducts(List<Product> products, String message) {
        String text = message == null ? "" : message.toLowerCase(Locale.ROOT);
        boolean light = text.contains("清淡") || text.contains("健康") || text.contains("不辣");
        boolean spicy = text.contains("辣");
        boolean fast = text.contains("快") || text.contains("赶时间");
        return products.stream()
                .sorted((a, b) -> Integer.compare(score(b, light, spicy, fast), score(a, light, spicy, fast)))
                .toList();
    }

    private int score(Product product, boolean light, boolean spicy, boolean fast) {
        int score = 0;
        String tags = product.getTasteTags() == null ? "" : product.getTasteTags();
        if (light && (tags.contains("清淡") || tags.contains("健康") || tags.contains("无糖"))) {
            score += 4;
        }
        if (spicy && tags.contains("辣")) {
            score += 4;
        }
        if (fast && product.getCookTime() != null && product.getCookTime() <= 8) {
            score += 3;
        }
        if (tags.contains("热销")) {
            score += 2;
        }
        if ("饮品".equals(product.getCategory())) {
            score += 1;
        }
        return score;
    }

    private AiOrderRecommendationVO.Plan composePlan(String name, String reason, List<Product> products, BigDecimal budget) {
        AiOrderRecommendationVO.Plan plan = new AiOrderRecommendationVO.Plan();
        plan.setName(name);
        plan.setReason(reason);
        BigDecimal total = BigDecimal.ZERO;
        for (Product product : products) {
            if (plan.getItems().size() >= 3) {
                break;
            }
            BigDecimal price = ProductVO.calcMemberPrice(product);
            if (budget.signum() > 0 && total.add(price).compareTo(budget) > 0 && !plan.getItems().isEmpty()) {
                continue;
            }
            AiOrderRecommendationVO.Item item = toPlanItem(product, 1);
            plan.getItems().add(item);
            total = total.add(item.getSubtotal());
        }
        plan.setTotalAmount(total.setScale(2, RoundingMode.HALF_UP));
        return plan;
    }

    private AiOrderRecommendationVO.Item toPlanItem(Product product, int quantity) {
        int safeQuantity = Math.max(1, quantity);
        BigDecimal unitPrice = ProductVO.calcMemberPrice(product);
        AiOrderRecommendationVO.Item item = new AiOrderRecommendationVO.Item();
        item.setProductId(product.getId());
        item.setProductName(product.getName());
        item.setName(product.getName());
        item.setCategory(product.getCategory());
        item.setQuantity(safeQuantity);
        item.setUnitPrice(unitPrice);
        item.setSubtotal(unitPrice.multiply(BigDecimal.valueOf(safeQuantity)).setScale(2, RoundingMode.HALF_UP));
        item.setCookTime(product.getCookTime());
        return item;
    }

    private BigDecimal extractBudget(String message) {
        if (message == null) {
            return BigDecimal.ZERO;
        }
        Matcher matcher = Pattern.compile("(\\d+(?:\\.\\d+)?)\\s*元").matcher(message);
        if (matcher.find()) {
            return new BigDecimal(matcher.group(1));
        }
        return BigDecimal.ZERO;
    }

    private List<Product> availableProducts() {
        return productMapper.selectList(new LambdaQueryWrapper<Product>()
                .eq(Product::getStatus, 1)
                .gt(Product::getStock, 0)
                .orderByDesc(Product::getStock));
    }

    private String extractJson(String content) {
        String text = content == null ? "" : content.trim();
        int start = text.indexOf('{');
        int end = text.lastIndexOf('}');
        if (start >= 0 && end > start) {
            return text.substring(start, end + 1);
        }
        return text;
    }

    private List<String> asStringList(Object value) {
        if (value instanceof List<?> list) {
            return list.stream().map(String::valueOf).toList();
        }
        if (value == null) {
            return List.of();
        }
        return List.of(String.valueOf(value));
    }

    private String resolveSessionId(String sessionId) {
        return sessionId == null || sessionId.isBlank() ? UUID.randomUUID().toString() : sessionId;
    }

    private void log(String sessionId, Long userId, String role, String content, String scene) {
        AiChatLog log = new AiChatLog();
        log.setSessionId(sessionId);
        log.setUserId(userId);
        log.setRole(role);
        log.setContent(content == null ? "" : content);
        log.setScene(scene);
        log.setCreateTime(LocalDateTime.now());
        aiChatLogMapper.insert(log);
    }

    private String safeWrite(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (Exception ex) {
            return String.valueOf(value);
        }
    }
}
