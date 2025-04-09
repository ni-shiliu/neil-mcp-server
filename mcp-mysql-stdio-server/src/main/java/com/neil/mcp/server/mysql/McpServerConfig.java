package com.neil.mcp.server.mysql;

import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.tool.method.MethodToolCallbackProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author nihao
 * @date 2025/4/9
 */
@Configuration
public class McpServerConfig {

    @Bean
    public ToolCallbackProvider toolCallbackProvider(@Autowired MysqlService mysqlService) {
        return MethodToolCallbackProvider.builder().toolObjects(mysqlService).build();
    }
}
