# mcp-mysql-stdio-server

## 使用

### 方案一：npm远程

#### 1. 编辑mcp.json配置
```json
{
  "mcpServers": {
    "mcp-mysql": {
      "isActive": true,
      "command": "npx",
      "args": [
        "-y",
        "@neil_nishiliu/mcp-mysql"
      ],
      "env": {
        "MYSQL_HOST": "your mysql host",
        "MYSQL_PORT": "your mysql port",
        "MYSQL_USERNAME": "your mysql username",
        "MYSQL_PASSWORD": "your mysql password",
        "MYSQL_DBNAME": "your mysql dbname"
      }
    }
  }
}
```

### 方案二：本地

#### 1. clone 工程

```shell
git clone https://github.com/ni-shiliu/neil-mcp-server.git
```

#### 2. package工程
```shell
cd /your path/neil-mcp-server/mcp-mysql-stdio-server
mvn clean package
```

#### 3.编辑mcp.json配置
```json
{
  "mcpServers": {
    "mcp-mysql": {
      "isActive": true,
      "command": "java",
      "args": [
        "-jar",
        "/your path/neil-mcp-server/mcp-mysql-stdio-server/target/mcp-mysql-stdio-server-0.0.1-SNAPSHOT.jar"
      ],
      "env": {
        "MYSQL_HOST": "your mysql host",
        "MYSQL_PORT": "your mysql port",
        "MYSQL_USERNAME": "your mysql username",
        "MYSQL_PASSWORD": "your mysql password",
        "MYSQL_DBNAME": "your mysql dbname"
      }
    }
  }
}
```