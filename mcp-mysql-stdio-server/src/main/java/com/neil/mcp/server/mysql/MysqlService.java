package com.neil.mcp.server.mysql;

import com.zaxxer.hikari.HikariDataSource;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.util.Strings;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class MysqlService {

    private static final String TABLE_NAME_KEY = "tableName";
    private static final String COLUMNS_KEY = "columns";
    private static final String COLUMN_NAME_KEY = "name";
    private static final String COLUMN_TYPE_KEY = "type";
    private static final String COLUMN_SIZE_KEY = "size";
    private static final String NULLABLE_KEY = "nullable";
    private static final String COMMENT_KEY = "comment";

    // 添加 volatile 保证可见性
    private volatile DataSource dataSource;

    private DataSource createDataSource() {
        String host = getEnvWithCheck("MYSQL_HOST");
        String port = getEnvWithCheck("MYSQL_PORT");
        String username = getEnvWithCheck("MYSQL_USERNAME");
        String password = getEnvWithCheck("MYSQL_PASSWORD");
        String dbName = getEnvWithCheck("MYSQL_DBNAME");

        String url = String.format("jdbc:mysql://%s:%s/%s?useUnicode=true&characterEncoding=UTF-8&useSSL=false",
                host, port, dbName);

        return DataSourceBuilder.create()
                .type(HikariDataSource.class)
                .url(url)
                .username(username)
                .password(password)
                .driverClassName("com.mysql.cj.jdbc.Driver")
                .build();
    }

    private String getEnvWithCheck(String varName) {
        String value = System.getenv(varName);
        if (Strings.isBlank(value)) {
            String msg = "Missing required environment variable: " + varName;
            log.error(msg);
            throw new IllegalStateException(msg);
        }
        return value;
    }

    public DataSource getDataSource() {
        if (dataSource == null) {
            synchronized (this) {
                if (dataSource == null) {
                    dataSource = createDataSource();
                }
            }
        }
        return dataSource;
    }

    @Tool(description = "execute query")
    public List<Map<String, Object>> executeQuery(String sql) {
        try (Connection conn = getDataSource().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet resultSet = stmt.executeQuery()) {

            List<Map<String, Object>> resultList = new ArrayList<>();
            ResultSetMetaData metaData = resultSet.getMetaData();
            int columnCount = metaData.getColumnCount();

            while (resultSet.next()) {
                Map<String, Object> row = new LinkedHashMap<>();
                for (int i = 1; i <= columnCount; i++) {
                    row.put(metaData.getColumnName(i), resultSet.getObject(i));
                }
                resultList.add(row);
            }
            return resultList;
        } catch (SQLException e) {
            log.error("SQL execution failed: {}", sql, e);
            throw new RuntimeException("Database operation failed", e);
        }
    }

    @Tool(description = "list all table structures")
    public List<Map<String, Object>> listAllTableStructures() {
        List<Map<String, Object>> result = new ArrayList<>();
        try (Connection conn = getDataSource().getConnection()) {
            DatabaseMetaData metaData = conn.getMetaData();
            String catalog = conn.getCatalog();

            try (ResultSet tables = metaData.getTables(catalog, null, "%", new String[]{"TABLE"})) {
                while (tables.next()) {
                    Map<String, Object> tableInfo = new LinkedHashMap<>();
                    String tableName = tables.getString("TABLE_NAME");

                    // 添加表基本信息
                    tableInfo.put(TABLE_NAME_KEY, tableName);

                    // 处理列信息
                    List<Map<String, Object>> columns = new ArrayList<>();
                    try (ResultSet cols = metaData.getColumns(catalog, null, tableName, "%")) {
                        while (cols.next()) {
                            Map<String, Object> column = new LinkedHashMap<>();
                            column.put(COLUMN_NAME_KEY, cols.getString("COLUMN_NAME"));
                            column.put(COLUMN_TYPE_KEY, cols.getString("TYPE_NAME"));
                            column.put(COLUMN_SIZE_KEY, cols.getInt("COLUMN_SIZE"));

                            int nullable = cols.getInt("NULLABLE");
                            column.put(NULLABLE_KEY, nullable == DatabaseMetaData.columnNullable ? "YES" : "NO");

                            String remarks = cols.getString("REMARKS");
                            if (remarks != null && !remarks.isBlank()) {
                                column.put(COMMENT_KEY, remarks);
                            }
                            columns.add(column);
                        }
                    }
                    tableInfo.put(COLUMNS_KEY, columns);
                    result.add(tableInfo);
                }
            }
        } catch (SQLException e) {
            log.error("Failed to retrieve table structures", e);
            throw new RuntimeException("Database operation failed", e);
        }
        return result;
    }

    @Tool(description = "describe table")
    public List<Map<String, Object>> describeTable(String tableName) {
        String sql = String.format("DESCRIBE %s", tableName);
        return executeQuery(sql);
    }

}
