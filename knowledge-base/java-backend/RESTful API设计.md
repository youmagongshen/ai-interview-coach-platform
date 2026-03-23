# RESTful API设计

## RESTful原则

1. **资源定位**：用URI表示资源，如 `/users`、`/orders`
2. **HTTP方法**：GET查询、POST创建、PUT更新、DELETE删除
3. **无状态**：每个请求包含所有必要信息
4. **统一接口**：使用标准HTTP状态码

## URL设计规范

### 命名
- 用名词复数：`/users` 而不是 `/user`
- 用小写字母：`/user-names` 而不是 `/userNames`
- 用连字符：`/user-names` 而不是 `/user_names`

### 层级
- 资源嵌套：`/users/123/orders`（用户123的订单）
- 最多2层嵌套，避免过深

### 示例
```
GET    /users          # 获取用户列表
GET    /users/123      # 获取指定用户
POST   /users          # 创建用户
PUT    /users/123      # 更新用户
DELETE /users/123      # 删除用户
GET    /users/123/orders  # 获取用户的订单
```

## HTTP状态码使用

### 成功
- 200 OK - 请求成功
- 201 Created - 创建成功
- 204 No Content - 删除成功

### 客户端错误
- 400 Bad Request - 请求参数错误
- 401 Unauthorized - 未认证
- 403 Forbidden - 无权限
- 404 Not Found - 资源不存在
- 422 Unprocessable Entity - 参数验证失败

### 服务器错误
- 500 Internal Server Error
- 503 Service Unavailable

## 请求响应规范

### 请求头
- Content-Type: application/json
- Authorization: Bearer token

### 响应格式
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "id": 1,
    "name": "张三"
  }
}
```

## 最佳实践

1. 版本控制：`/api/v1/users`
2. 分页：`/users?page=1&size=10`
3. 排序：`/users?sort=name,desc`
4. 过滤：`/users?status=active`
5. 字段选择：`/users?fields=id,name`
6. 错误响应：包含错误码和详细message
