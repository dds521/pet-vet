package com.petvet.common.mybatis.interceptor;

import lombok.extern.slf4j.Slf4j;
import net.sf.jsqlparser.expression.BinaryExpression;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.LongValue;
import net.sf.jsqlparser.expression.operators.conditional.AndExpression;
import net.sf.jsqlparser.expression.operators.relational.EqualsTo;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.delete.Delete;
import net.sf.jsqlparser.statement.select.*;
import net.sf.jsqlparser.statement.update.Update;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.plugin.*;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;

/**
 * is_void 查询拦截器
 * 
 * 自动在查询 SQL 中添加 is_void = 0 条件，确保只查询未删除的数据
 * 支持 SELECT、UPDATE、DELETE 语句
 * 
 * 注意：此拦截器只处理自定义 SQL（@Select、@Update、@Delete 注解或 XML 中的 SQL），
 * MyBatis Plus 的 BaseMapper 方法已经通过 @TableLogic 注解自动处理逻辑删除
 * 
 * @author daidasheng
 * @date 2024-12-27
 */
@Slf4j
@Intercepts({
    @Signature(type = Executor.class, method = "query", args = {MappedStatement.class, Object.class, RowBounds.class, ResultHandler.class}),
    @Signature(type = Executor.class, method = "update", args = {MappedStatement.class, Object.class})
})
public class IsVoidQueryInterceptor implements Interceptor {
    
    /**
     * is_void 字段名
     */
    private static final String IS_VOID_COLUMN = "is_void";
    
    /**
     * 未删除的值
     */
    private static final long NOT_DELETED_VALUE = 0L;
    
    /**
     * 拦截方法
     * 
     * @param invocation 方法调用
     * @return 执行结果
     * @throws Throwable 异常
     * @author daidasheng
     * @date 2024-12-27
     */
    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        MappedStatement ms = (MappedStatement) invocation.getArgs()[0];
        Object parameter = invocation.getArgs()[1];
        
        // 获取 BoundSql
        BoundSql boundSql = ms.getBoundSql(parameter);
        String sql = boundSql.getSql();
        
        if (sql == null || sql.trim().isEmpty()) {
            return invocation.proceed();
        }
        
        // 检查是否是查询或更新操作
        String sqlCommandType = ms.getSqlCommandType().name();
        
        try {
            // 解析 SQL
            Statement statement = net.sf.jsqlparser.parser.CCJSqlParserUtil.parse(sql);
            
            boolean modified = false;
            
            // 处理 SELECT 语句
            if (statement instanceof Select) {
                Select select = (Select) statement;
                PlainSelect plainSelect = select.getPlainSelect();
                if (plainSelect != null) {
                    modified = addIsVoidCondition(plainSelect);
                    if (modified) {
                        String newSql = select.toString();
                        replaceSql(boundSql, newSql);
                        log.debug("SELECT SQL 已添加 is_void = 0 条件: {}", newSql);
                    }
                }
            }
            // 处理 UPDATE 语句
            else if (statement instanceof Update) {
                Update update = (Update) statement;
                modified = addIsVoidConditionToUpdate(update);
                if (modified) {
                    String newSql = update.toString();
                    replaceSql(boundSql, newSql);
                    log.debug("UPDATE SQL 已添加 is_void = 0 条件: {}", newSql);
                }
            }
            // 处理 DELETE 语句
            else if (statement instanceof Delete) {
                Delete delete = (Delete) statement;
                modified = addIsVoidConditionToDelete(delete);
                if (modified) {
                    String newSql = delete.toString();
                    replaceSql(boundSql, newSql);
                    log.debug("DELETE SQL 已添加 is_void = 0 条件: {}", newSql);
                }
            }
        } catch (Exception e) {
            // 如果 SQL 解析失败，记录日志但不影响执行
            log.warn("解析 SQL 失败，跳过 is_void 条件添加: {}", sql, e);
        }
        
        return invocation.proceed();
    }
    
    /**
     * 为 UPDATE 语句添加 is_void = 0 条件
     * 
     * @param update UPDATE 语句
     * @return 是否修改了 SQL
     * @author daidasheng
     * @date 2024-12-27
     */
    private boolean addIsVoidConditionToUpdate(Update update) {
        Expression where = update.getWhere();
        
        // 检查是否已经存在 is_void 条件
        if (hasIsVoidCondition(where)) {
            log.debug("UPDATE SQL 中已存在 is_void 条件，跳过添加");
            return false;
        }
        
        // 创建 is_void = 0 条件
        EqualsTo isVoidCondition = createIsVoidCondition(update.getTable());
        if (where != null) {
            update.setWhere(new AndExpression(where, isVoidCondition));
        } else {
            update.setWhere(isVoidCondition);
        }
        return true;
    }
    
    /**
     * 为 DELETE 语句添加 is_void = 0 条件
     * 
     * @param delete DELETE 语句
     * @return 是否修改了 SQL
     * @author daidasheng
     * @date 2024-12-27
     */
    private boolean addIsVoidConditionToDelete(Delete delete) {
        Expression where = delete.getWhere();
        
        // 检查是否已经存在 is_void 条件
        if (hasIsVoidCondition(where)) {
            log.debug("DELETE SQL 中已存在 is_void 条件，跳过添加");
            return false;
        }
        
        // 创建 is_void = 0 条件
        EqualsTo isVoidCondition = createIsVoidCondition(delete.getTable());
        if (where != null) {
            delete.setWhere(new AndExpression(where, isVoidCondition));
        } else {
            delete.setWhere(isVoidCondition);
        }
        return true;
    }
    
    /**
     * 为 SELECT 语句添加 is_void = 0 条件
     * 
     * @param plainSelect SELECT 语句
     * @return 是否修改了 SQL
     * @author daidasheng
     * @date 2024-12-27
     */
    private boolean addIsVoidCondition(PlainSelect plainSelect) {
        // 获取主表
        Table table = null;
        if (plainSelect.getFromItem() instanceof Table) {
            table = (Table) plainSelect.getFromItem();
        }
        if (table == null) {
            return false;
        }
        
        // 检查是否已经存在 is_void 条件
        Expression where = plainSelect.getWhere();
        if (hasIsVoidCondition(where)) {
            log.debug("SELECT SQL 中已存在 is_void 条件，跳过添加");
            return false;
        }
        
        // 创建 is_void = 0 条件
        EqualsTo isVoidCondition = createIsVoidCondition(table);
        
        // 添加到 WHERE 子句
        if (where != null) {
            plainSelect.setWhere(new AndExpression(where, isVoidCondition));
        } else {
            plainSelect.setWhere(isVoidCondition);
        }
        return true;
    }
    
    /**
     * 创建 is_void = 0 条件表达式
     * 
     * @param table 表对象
     * @return 条件表达式
     * @author daidasheng
     * @date 2024-12-27
     */
    private EqualsTo createIsVoidCondition(Table table) {
        Column column = new Column();
        if (table.getAlias() != null) {
            column.setTable(table);
        }
        column.setColumnName(IS_VOID_COLUMN);
        
        EqualsTo equalsTo = new EqualsTo();
        equalsTo.setLeftExpression(column);
        equalsTo.setRightExpression(new LongValue(NOT_DELETED_VALUE));
        
        return equalsTo;
    }
    
    /**
     * 创建插件代理对象
     * 
     * @param target 目标对象
     * @return 代理对象
     * @author daidasheng
     * @date 2024-12-27
     */
    @Override
    public Object plugin(Object target) {
        return Plugin.wrap(target, this);
    }
    
    /**
     * 设置拦截器属性
     * 
     * @param properties 属性
     * @author daidasheng
     * @date 2024-12-27
     */
    @Override
    public void setProperties(java.util.Properties properties) {
        // 可以在这里设置配置属性
    }
    
    /**
     * 检查 WHERE 条件中是否已经包含 is_void 条件
     * 
     * @param expression WHERE 表达式
     * @return 是否已包含
     * @author daidasheng
     * @date 2024-12-27
     */
    private boolean hasIsVoidCondition(Expression expression) {
        if (expression == null) {
            return false;
        }
        
        // 如果是 EqualsTo 表达式，检查是否是 is_void 字段
        if (expression instanceof EqualsTo) {
            EqualsTo equalsTo = (EqualsTo) expression;
            if (equalsTo.getLeftExpression() instanceof Column) {
                Column column = (Column) equalsTo.getLeftExpression();
                if (IS_VOID_COLUMN.equalsIgnoreCase(column.getColumnName())) {
                    return true;
                }
            }
        }
        
        // 如果是 AndExpression 或 OrExpression，递归检查
        if (expression instanceof BinaryExpression) {
            BinaryExpression binaryExpression = (BinaryExpression) expression;
            return hasIsVoidCondition(binaryExpression.getLeftExpression()) 
                || hasIsVoidCondition(binaryExpression.getRightExpression());
        }
        
        return false;
    }
    
    /**
     * 替换 BoundSql 中的 SQL
     * 
     * @param boundSql BoundSql 对象
     * @param newSql 新的 SQL
     * @author daidasheng
     * @date 2024-12-27
     */
    private void replaceSql(BoundSql boundSql, String newSql) {
        try {
            // 使用反射替换 SQL
            java.lang.reflect.Field field = BoundSql.class.getDeclaredField("sql");
            field.setAccessible(true);
            field.set(boundSql, newSql);
        } catch (Exception e) {
            log.error("替换 SQL 失败", e);
        }
    }
}

