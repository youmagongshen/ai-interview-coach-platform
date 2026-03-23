# HTTP与HTTPS

## HTTP与HTTPS的区别

| 区别 | HTTP | HTTPS |
|------|------|-------|
| 安全性 | 明文传输 | SSL/TLS加密传输 |
| 端口 | 80 | 443 |
| 证书 | 不需要 | 需要CA证书 |
| SEO | 无 | 有优势 |
| 速度 | 稍快 | 稍慢（加密开销） |

## HTTPS加密原理

1. **客户端发送请求**：告诉服务器支持的加密算法
2. **服务器响应**：返回证书（包含公钥）
3. **客户端验证证书**：检查证书是否有效
4. **生成随机密钥**：用公钥加密随机密钥发送给服务器
5. **建立安全通道**：双方用随机密钥进行对称加密通信

## 常用HTTP状态码

- **2xx**：成功（200 OK, 201 Created）
- **3xx**：重定向（301 Moved, 304 Not Modified）
- **4xx**：客户端错误（400 Bad Request, 401 Unauthorized, 403 Forbidden, 404 Not Found）
- **5xx**：服务器错误（500 Internal Server Error, 503 Service Unavailable）

## HTTP方法

- GET：获取资源
- POST：创建资源
- PUT：更新资源
- DELETE：删除资源
- PATCH：部分更新
