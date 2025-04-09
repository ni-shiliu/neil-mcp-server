package com.neil.mcp.server.mysql;

import io.modelcontextprotocol.client.McpClient;
import io.modelcontextprotocol.client.transport.ServerParameters;
import io.modelcontextprotocol.client.transport.StdioClientTransport;
import io.modelcontextprotocol.spec.McpSchema;
import org.springframework.ai.util.json.JsonParser;

import java.util.HashMap;
import java.util.Map;

/**
 * @author nihao
 * @date 2025/4/8
 */
public class ClientStdio {

    public static void main(String[] args) {
        Map<String, String> env = new HashMap<>();
        env.put("MYSQL_HOST", "YOUR_MYSQL_HOST");
        env.put("MYSQL_PORT", "YOUR_MYSQL_PORT");
        env.put("MYSQL_USERNAME", "YOUR_MYSQL_USERNAME");
        env.put("MYSQL_PASSWORD", "YOUR_MYSQL_PASSWORD");
        env.put("MYSQL_DBNAME", "YOUR_MYSQL_DBNAME");
        ServerParameters sp = ServerParameters.builder("java")
                .args("-jar",
                        "mcp-mysql-stdio-server/target/mcp-mysql-stdio-server-0.0.1-SNAPSHOT.jar")
                .env(env)
                .build();
        var transport = new StdioClientTransport(sp);
        var client = McpClient.sync(transport).build();
        client.initialize();

        McpSchema.ListToolsResult toolsList = client.listTools();
        System.out.println("Available Tools = " + toolsList);

        McpSchema.CallToolResult sqlResult = client.callTool(new McpSchema.CallToolRequest("executeQuery",
                Map.of("sql", "select * from fl_user where id = 13;")));

        System.out.println("sql executeQuery: " + JsonParser.toJson(sqlResult));

        McpSchema.CallToolResult sqlResult2 = client.callTool(new McpSchema.CallToolRequest("listAllTableStructures", null));
        System.out.println("sql listAllTableStructures: " + JsonParser.toJson(sqlResult2));

        client.closeGracefully();

    }
}
