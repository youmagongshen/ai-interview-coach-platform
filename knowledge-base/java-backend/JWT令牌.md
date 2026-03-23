# JWT令牌

## 什么是JWT

JWT（JSON Web Token）是一种开放标准，用于在各方之间安全传输JSON信息。

## JWT组成结构

JWT由三部分组成，用 `.` 分隔：
```
xxxxx.yyyyy.zzzzz
```

1. **Header（头部）**
   - 算法类型（如HS256）
   - 类型（JWT）
   ```json
   {
     "alg": "HS256",
     "typ": "JWT"
   }
   ```

2. **Payload（载荷）**
   - 存放有效信息
   ```json
   {
     "sub": "1234567890",
     "name": "张三",
     "role": "admin",
     "exp": 1700000000
   }
   ```
   - 常见声明：
     - iss：签发者
     - sub：主题
     - exp：过期时间
     - iat：签发时间

3. **Signature（签名）**
   - Header + Payload + 密钥，使用指定算法加密

## JWT vs Session

| 特性 | JWT | Session |
|------|-----|---------|
| 存储位置 | 客户端 | 服务端 |
| 分布式 | 支持 | 需共享存储 |
| 跨域 | 方便 | 麻烦 |
| 安全性 | 需防篡改 | 相对安全 |
| 性能 | 无服务端查询 | 需要查询 |

## JWT优点

1. 无状态，服务端无需存储
2. 适合分布式系统
3. 跨语言支持好
4. 体积小，传输快

## JWT缺点

1. 无法主动失效（需要黑名单或Redis）
2.  payload明文可读（不要存敏感信息）
3. _token_较长

## 使用场景

- 用户登录
- API鉴权
- 移动端认证
- SSO单点登录

## 安全建议

1. 过期时间不要过长
2. 使用HTTPS传输
3. payload不存敏感信息
4. 验证签名
5. 考虑使用Refresh Token
