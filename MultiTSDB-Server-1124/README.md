# MultiTSDB-Server-1124

多时序数据库统一接口服务，支持IoTDB和TDEngine。

## 功能特性

- 支持多种时序数据库（IoTDB、TDEngine）
- 统一的API接口，屏蔽不同数据库的差异
- 支持树模型和表模型（IoTDB）
- 自动创建默认数据库cloud_platform
- 提供RESTful API和Swagger文档

## 配置说明

### IoTDB配置
- 默认端口: 6667
- 默认账号: root/root
- 支持树模型和表模型切换

### TDEngine配置
- 默认端口: 6041
- 默认账号: root/taosdata

## 启动服务

```bash
cd MultiTSDB-Server-1124
mvn spring-boot:run
```

服务启动后访问:
- Swagger UI: http://localhost:1124/swagger-ui.html
- API文档: http://localhost:1124/v2/api-docs

## 数据模型说明

统一写入请求中的单条数据结构如下：

```json
{
  "device": "device_001",
  "measurement": "temperature",
  "timestamp": 1677648000000,
  "fields": {
    "value": 25.5,
    "unit": "celsius"
  },
  "tags": {
    "location": "room_1",
    "sensor_type": "temperature_sensor"
  }
}
```

字段含义：
- `device`: 设备标识。IoTDB树模型下会参与组成设备路径；TDEngine中会作为普通列保存。
- `measurement`: 测点名或表名。
- `timestamp`: 毫秒时间戳。
- `fields`: 业务字段，支持数值、布尔、字符串。
- `tags`: 标签字段。IoTDB表模型下可作为标签使用；当前TDEngine实现中会作为普通列落库。

统一请求中的公共字段：

```json
{
  "tsdbType": "iotdb",
  "database": "cloud_platform"
}
```

- `tsdbType`: 可选值为 `iotdb` 或 `tdengine`
- `database`: 逻辑数据库名，默认值为 `cloud_platform`

## API接口说明

### 1. 写入数据
POST /api/tsdb/write

请求体示例:
```json
{
  "tsdbType": "iotdb",
  "database": "cloud_platform",
  "dataList": [
    {
      "device": "device_001",
      "measurement": "temperature",
      "timestamp": 1677648000000,
      "fields": {
        "value": 25.5,
        "unit": "celsius"
      },
      "tags": {
        "location": "room_1"
      }
    }
  ]
}
```

curl示例:
```bash
curl -X POST 'http://localhost:1124/api/tsdb/write' \
  -H 'Content-Type: application/json' \
  -d '{
    "tsdbType": "tdengine",
    "database": "cloud_platform",
    "dataList": [
      {
        "device": "device_001",
        "measurement": "temperature",
        "timestamp": 1774106000000,
        "fields": {
          "value": 25.5,
          "unit": "celsius"
        },
        "tags": {
          "location": "room_1",
          "sensor_type": "temperature_sensor"
        }
      }
    ]
  }'
```

响应示例:
```json
{
  "code": 200,
  "message": "数据写入成功",
  "data": true
}
```

### 2. 查询数据
POST /api/tsdb/query

请求体示例:
```json
{
  "tsdbType": "iotdb",
  "database": "cloud_platform",
  "sql": "SELECT * FROM device_001"
}
```

说明：
- IoTDB常用查询示例：`SELECT * FROM device_001`
- TDEngine支持两种写法：
  - 仅传表名：`temperature`
  - 完整SQL：`SELECT * FROM temperature LIMIT 5`

curl示例:
```bash
curl -X POST 'http://localhost:1124/api/tsdb/query' \
  -H 'Content-Type: application/json' \
  -d '{
    "tsdbType": "tdengine",
    "database": "cloud_platform",
    "sql": "SELECT * FROM temperature LIMIT 5"
  }'
```

响应示例:
```json
{
  "code": 200,
  "message": "查询成功",
  "data": {
    "columns": ["ts", "device", "unit", "value", "location", "sensor_type"],
    "rows": [
      {
        "ts": "2026-03-21 23:20:13",
        "device": "device_001",
        "unit": "celsius",
        "value": 25.5,
        "location": "room_1",
        "sensor_type": "temperature_sensor"
      }
    ],
    "rowCount": 1,
    "message": "Query executed successfully",
    "success": true
  }
}
```

### 3. 按时间范围查询
POST /api/tsdb/queryByTimeRange

请求体示例:
```json
{
  "tsdbType": "tdengine",
  "database": "cloud_platform",
  "measurement": "temperature",
  "startTime": 0,
  "endTime": 4102444800000,
  "limit": 10
}
```

curl示例:
```bash
curl -X POST 'http://localhost:1124/api/tsdb/queryByTimeRange' \
  -H 'Content-Type: application/json' \
  -d '{
    "tsdbType": "iotdb",
    "database": "cloud_platform",
    "measurement": "device_001",
    "startTime": 0,
    "endTime": 4102444800000,
    "limit": 10
  }'
```

响应示例:
```json
{
  "code": 200,
  "message": "查询成功",
  "data": {
    "columns": ["Time", "root.cloud_platform.device_001.unit", "root.cloud_platform.device_001.value"],
    "rows": [
      {
        "Time": 1774105847330,
        "root.cloud_platform.device_001.unit": "celsius",
        "root.cloud_platform.device_001.value": 25.5
      }
    ],
    "rowCount": 1,
    "message": "Query executed successfully",
    "success": true
  }
}
```

### 4. 直接执行SQL
POST /api/tsdb/executeSql

请求体示例:
```json
{
  "tsdbType": "iotdb",
  "database": "cloud_platform",
  "sql": "SHOW DATABASES"
}
```

说明：
- 这个接口会直接执行传入的 SQL
- SQL 执行报错时，会直接返回底层数据库报错信息
- IoTDB 示例：
  - 成功：`SHOW DATABASES`
  - 失败：`BAD SQL`
- TDEngine 示例：
  - 成功：`SELECT * FROM temperature LIMIT 1`
  - 失败：`BAD SQL`

curl示例:
```bash
curl -X POST 'http://localhost:1124/api/tsdb/executeSql' \
  -H 'Content-Type: application/json' \
  -d '{
    "tsdbType": "tdengine",
    "database": "cloud_platform",
    "sql": "SELECT * FROM temperature LIMIT 1"
  }'
```

成功响应示例:
```json
{
  "code": 200,
  "message": "SQL执行成功",
  "data": {
    "columns": ["ts", "device", "unit", "value", "location", "sensor_type"],
    "rows": [
      {
        "ts": "2026-03-21 23:17:12",
        "device": "device_001",
        "unit": "celsius",
        "value": 25.5,
        "location": "room_1",
        "sensor_type": "temperature_sensor"
      }
    ],
    "rowCount": 1,
    "message": "Query executed successfully",
    "success": true
  }
}
```

失败响应示例:
```json
{
  "code": 500,
  "message": "TDengine ERROR (0x2600): sql: BAD SQL, desc: syntax error near \"bad sql\"",
  "data": null
}
```

### 5. 创建数据库
POST /api/tsdb/createDatabase?tsdbType=iotdb&database=test_db

curl示例:
```bash
curl -X POST 'http://localhost:1124/api/tsdb/createDatabase?tsdbType=tdengine&database=test_db'
```

响应示例:
```json
{
  "code": 200,
  "message": "数据库创建成功",
  "data": true
}
```

### 6. 删除数据库
POST /api/tsdb/dropDatabase?tsdbType=iotdb&database=test_db

curl示例:
```bash
curl -X POST 'http://localhost:1124/api/tsdb/dropDatabase?tsdbType=tdengine&database=test_db'
```

响应示例:
```json
{
  "code": 200,
  "message": "数据库删除成功",
  "data": true
}
```

### 7. 健康检查
GET /api/tsdb/health

curl示例:
```bash
curl 'http://localhost:1124/api/tsdb/health'
```

响应示例:
```json
{
  "code": 200,
  "message": "MultiTSDB服务运行正常",
  "data": "OK"
}
```

### 8. 获取可用适配器
GET /api/tsdb/adapters

curl示例:
```bash
curl 'http://localhost:1124/api/tsdb/adapters'
```

响应示例:
```json
{
  "code": 200,
  "message": "获取适配器列表成功",
  "data": ["iotdb", "tdengine"]
}
```

## 示例接口

### 写入示例数据
POST /api/example/writeSampleData?tsdbType=iotdb&database=cloud_platform

curl示例:
```bash
curl -X POST 'http://localhost:1124/api/example/writeSampleData?tsdbType=tdengine&database=cloud_platform'
```

### 查询示例数据
POST /api/example/querySampleData?tsdbType=iotdb&database=cloud_platform

curl示例:
```bash
curl -X POST 'http://localhost:1124/api/example/querySampleData?tsdbType=iotdb&database=cloud_platform&sql=SELECT%20*%20FROM%20device_001'
```

### 批量写入示例数据
POST /api/example/batchWriteSampleData?tsdbType=iotdb&database=cloud_platform&count=10

curl示例:
```bash
curl -X POST 'http://localhost:1124/api/example/batchWriteSampleData?tsdbType=iotdb&database=cloud_platform&count=10'
```

## 当前实现说明

- IoTDB默认使用树模型写入，逻辑数据库 `cloud_platform` 会被映射为 `root.cloud_platform`
- IoTDB查询结果中的字符串字段已转换为普通字符串返回
- TDEngine当前实现使用 `TAOS-RS` 连接方式
- TDEngine按 `measurement` 自动建普通表，并自动补充新增字段列
- TDEngine中 `device` 和 `tags` 会作为普通列保存，便于统一接口读写

## 架构设计

```
┌─────────────┐
│ Controller  │
└──────┬──────┘
       │
┌──────▼──────┐
│  Service    │
└──────┬──────┘
       │
┌──────▼──────┐
│  Adapter    │
│ (抽象层)     │
└──────┬──────┘
       │
   ┌───┴───┐
   │       │
┌──▼───┐ ┌─▼─────┐
│IoTDB │ │TD     │
│Adapter│ │Engine │
└──────┘ └───────┘
```

## 核心类说明

- **TSDBAdapter**: 时序数据库抽象接口
- **IoTDBAdapter**: IoTDB适配器实现
- **TDEngineAdapter**: TDEngine适配器实现
- **TSDBService**: 业务逻辑服务层
- **TSDBController**: REST API控制器
- **TSDBConfig**: 配置类，负责初始化连接
