# MultiTSDB-Server-1124

面向 IoTDB 和 TDEngine 的轻量时序库接口服务。

当前版本只保留必要的服务端结构：
- `controller`
- `service`
- `dto`
- `application.yml`

底层多时序库接入能力已经抽到公共 starter `cloud-perfume-multitsdb-spring-boot-starter` 中，`MultiTSDB-Server-1124` 只负责提供清晰的业务接口。

## 功能概览

- 提供 IoTDB 读写接口
- 提供 TDEngine 读写接口
- 两个时序库都提供直接执行 SQL 的接口
- IoTDB 批量写入时，starter 会自动优先尝试 `Tablet` 批量写入
- 当 `Tablet` 不适用或执行失败时，会自动回退到通用写入方式
- 调用方不需要显式知道或选择 `Tablet`
- `timestamp` 支持缺省，未传时会自动补当前时间
- `database` 支持缺省，未传时会回退到配置里的 `tsdb.default-database`
- IoTDB 树模型下会把 `tags` 也当普通字段一并写入，尽量与 TDEngine 保持字段一致

## 目录结构

```text
src/main/java/com/alandevise/multitsdb
├── MultiTsdbApplication.java
├── controller
│   ├── IoTDBController.java
│   ├── SystemController.java
│   └── TDEngineController.java
├── dto
│   ├── QueryByTimeRangeRequest.java
│   ├── QueryDataRequest.java
│   ├── Result.java
│   └── WriteDataRequest.java
└── service
    ├── BaseTSDBService.java
    ├── IoTDBService.java
    └── TDEngineService.java
```

## 配置说明

配置文件位置：

`src/main/resources/application.yml`

关键配置：

```yaml
server:
  port: 1124

tsdb:
  default-type: iotdb
  default-database: cloud_platform
  iotdb:
    enabled: true
    host: 127.0.0.1
    port: 6667
    username: root
    password: root
    use-table-model: false
  tdengine:
    enabled: true
    host: 127.0.0.1
    port: 6041
    username: root
    password: taosdata
```

说明：

- `tsdb.default-type`: 公共 starter 默认使用的时序库类型，默认是 `iotdb`
- `tsdb.default-database`: 默认数据库，默认是 `cloud_platform`
- `iotdb.use-table-model`: `false` 表示树模型，`true` 表示表模型

## 启动方式

先编译：

```bash
JAVA_HOME=/Library/Java/JavaVirtualMachines/jdk1.8.0_201.jdk/Contents/Home \
/Applications/IntelliJ\ IDEA.app/Contents/plugins/maven/lib/maven3/bin/mvn \
-pl Common-Module/cloud-perfume-multitsdb-spring-boot-starter,MultiTSDB-Server-1124 \
-am -DskipTests install
```

再在 `MultiTSDB-Server-1124` 目录下生成运行时 classpath：

```bash
JAVA_HOME=/Library/Java/JavaVirtualMachines/jdk1.8.0_201.jdk/Contents/Home \
/Applications/IntelliJ\ IDEA.app/Contents/plugins/maven/lib/maven3/bin/mvn \
-q -DskipTests dependency:build-classpath -Dmdep.outputFile=cp.txt
```

启动服务：

```bash
JAVA_HOME=/Library/Java/JavaVirtualMachines/jdk1.8.0_201.jdk/Contents/Home \
java -cp "target/classes:$(cat cp.txt)" com.alandevise.multitsdb.MultiTsdbApplication
```

启动后可访问：

- 系统健康检查：`GET /api/system/health`
- IoTDB 接口前缀：`/api/iotdb`
- TDEngine 接口前缀：`/api/tdengine`

## 数据模型

写入请求中的单条数据结构：

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

字段说明：

- `device`: 设备标识。IoTDB 树模型下会参与组成设备路径；TDEngine 中会作为普通列保存
- `measurement`: 测点名或表名
- `timestamp`: 毫秒时间戳。可不传，不传时 starter 会自动补当前时间
- `fields`: 业务字段，支持数值、布尔、字符串
- `tags`: 标签字段。在当前默认的 IoTDB 树模型下，也会作为普通字段一起落库；TDEngine 中同样会作为普通列保存

公共请求字段：

```json
{
  "database": "cloud_platform"
}
```

说明：

- 新版服务按接口路径区分时序库类型，所以请求体中不再需要 `tsdbType`
- `database` 为空时会回退到配置里的 `tsdb.default-database`
- 如果配置里也没指定，则 starter 默认会回退到 `cloud_platform`

## 接口说明

### 1. 系统健康检查

`GET /api/system/health`

示例响应：

```json
{
  "code": 200,
  "message": "MultiTSDB 服务运行正常",
  "data": "OK"
}
```

## IoTDB 接口

### 1. 写入数据

`POST /api/iotdb/write`

请求体示例：

```json
{
  "database": "cloud_platform",
  "dataList": [
    {
      "device": "device_001",
      "measurement": "temperature",
      "fields": {
        "value": 21.1,
        "unit": "celsius"
      },
      "tags": {
        "location": "r1"
      }
    }
  ]
}
```

curl 示例：

```bash
curl -X POST 'http://localhost:1124/api/iotdb/write' \
  -H 'Content-Type: application/json' \
  -d '{
    "database": "cloud_platform",
    "dataList": [
      {
        "device": "device_001",
        "measurement": "temperature",
        "fields": {
          "value": 21.1,
          "unit": "celsius"
        },
        "tags": {
          "location": "r1"
        }
      }
    ]
  }'
```

参数说明：

- `device`: 必填
- `measurement`: 必填
- `fields`: 建议必填，至少应包含一组业务字段
- `timestamp`: 选填，未传时自动使用当前时间
- `tags`: 选填，不传也可以正常写入
- `database`: 选填，未传时优先用 `tsdb.default-database`，再兜底到 `cloud_platform`

### 2. 查询数据

`POST /api/iotdb/query`

请求体示例：

```json
{
  "database": "cloud_platform",
  "sql": "SELECT * FROM device_001"
}
```

### 3. 按时间范围查询

`POST /api/iotdb/queryByTimeRange`

请求体示例：

```json
{
  "database": "cloud_platform",
  "measurement": "device_001",
  "startTime": 0,
  "endTime": 4102444800000,
  "limit": 10
}
```

### 4. 直接执行 SQL

`POST /api/iotdb/executeSql`

请求体示例：

```json
{
  "database": "cloud_platform",
  "sql": "SHOW DATABASES"
}
```

说明：

- 该接口会直接执行传入的 IoTDB SQL
- 写入接口内部会自动判断是否适合使用 `Tablet`
- 当批量数据满足同设备、同 schema 场景时，会自动走更优的 `Tablet` 写入
- 在默认树模型下，`tags` 也会以普通字段形式一并写入，因此查询结果中可以看到 `location`、`sensor_type` 这类列

## TDEngine 接口

### 1. 写入数据

`POST /api/tdengine/write`

请求体示例：

```json
{
  "database": "cloud_platform",
  "dataList": [
    {
      "device": "device_001",
      "measurement": "temperature_test",
      "fields": {
        "value": 26.4,
        "unit": "celsius"
      },
      "tags": {
        "location": "r2",
        "sensor_type": "temperature_sensor"
      }
    }
  ]
}
```

参数说明：

- `device`: 必填
- `measurement`: 必填
- `fields`: 建议必填，至少应包含一组业务字段
- `timestamp`: 选填，未传时自动使用当前时间
- `tags`: 选填，不传也可以正常写入
- `database`: 选填，未传时优先用 `tsdb.default-database`，再兜底到 `cloud_platform`

### 2. 查询数据

`POST /api/tdengine/query`

请求体示例：

```json
{
  "database": "cloud_platform",
  "sql": "SELECT * FROM temperature_test LIMIT 5"
}
```

### 3. 按时间范围查询

`POST /api/tdengine/queryByTimeRange`

请求体示例：

```json
{
  "database": "cloud_platform",
  "measurement": "temperature_test",
  "startTime": 0,
  "endTime": 4102444800000,
  "limit": 10
}
```

### 4. 直接执行 SQL

`POST /api/tdengine/executeSql`

请求体示例：

```json
{
  "database": "cloud_platform",
  "sql": "SHOW DATABASES"
}
```

## 已验证能力

本次重构后，已经做过实际启动和接口回归，确认以下能力正常：

- `GET /api/system/health`
- `GET /api/iotdb/health`
- `GET /api/tdengine/health`
- IoTDB 写入、查询、时间范围查询、执行 SQL
- TDEngine 写入、查询、时间范围查询、执行 SQL
- IoTDB 批量写入自动命中 `Tablet` 路径
- `timestamp` 缺省时自动补当前时间
- `database` 缺省时自动回退到 `cloud_platform`
- IoTDB 树模型下 `tags` 与 TDEngine 一样可作为普通字段查询到

## 说明

- 如果后续还要扩展更多时序库，建议继续把适配器和自动装配能力放在 starter 中
- `MultiTSDB-Server-1124` 建议始终保持为轻量接口层，不再回填底层适配器实现
