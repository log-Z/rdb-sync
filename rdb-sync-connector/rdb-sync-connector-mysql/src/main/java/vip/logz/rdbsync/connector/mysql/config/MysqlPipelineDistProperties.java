package vip.logz.rdbsync.connector.mysql.config;

import vip.logz.rdbsync.common.config.SemanticOptions;
import vip.logz.rdbsync.connector.jdbc.config.JdbcExactlyOncePipelineDistProperties;
import vip.logz.rdbsync.connector.jdbc.config.JdbcPipelineDistProperties;
import vip.logz.rdbsync.connector.mysql.rule.Mysql;

/**
 * MySQL管道目标属性
 *
 * @author logz
 * @date 2024-01-09
 */
public class MysqlPipelineDistProperties extends JdbcPipelineDistProperties {

    /** 精确同步一次的管道目标属性 */
    private final JdbcExactlyOncePipelineDistProperties exactlyOnceProps = new JdbcExactlyOncePipelineDistProperties();

    /**
     * 语义保证
     * @see SemanticOptions#EXACTLY_ONCE
     * @see SemanticOptions#AT_LEAST_ONCE
     */
    private String semantic;

    /**
     * 获取端口
     */
    @Override
    public Integer getPort() {
        Integer port = super.getPort();
        return port != null ? port : MysqlOptions.DEFAULT_PORT;
    }

    /**
     * 获取用户名
     */
    @Override
    public String getUsername() {
        return username != null ? username : MysqlOptions.DEFAULT_USERNAME;
    }

    /**
     * 获取密码
     */
    @Override
    public String getPassword() {
        return password != null ? password : MysqlOptions.DEFAULT_PASSWORD;
    }

    /**
     * 获取语义保证
     */
    public String getSemantic() {
        return semantic;
    }

    /**
     * 设置语义保证
     * @param semantic 语义保证
     */
    public void setSemantic(String semantic) {
        this.semantic = semantic;
    }

    /**
     * 获取精确一次属性：事务提交尝试次数
     */
    public Integer getTxMaxCommitAttempts() {
        return exactlyOnceProps.getTxMaxCommitAttempts();
    }

    /**
     * 设置精确一次属性：事务提交尝试次数
     * @param txMaxCommitAttempts 精确一次属性：事务提交尝试次数
     */
    public void setTxMaxCommitAttempts(Integer txMaxCommitAttempts) {
        exactlyOnceProps.setTxMaxCommitAttempts(txMaxCommitAttempts);
    }

    /**
     * 获取精确一次属性：事务超时秒数
     */
    public Integer getTxTimeoutSeconds() {
        return exactlyOnceProps.getTxTimeoutSeconds();
    }

    /**
     * 设置精确一次属性：事务超时秒数
     * @param txTimeoutSeconds 精确一次属性：事务超时秒数
     */
    public void setTxTimeoutSeconds(Integer txTimeoutSeconds) {
        exactlyOnceProps.setTxTimeoutSeconds(txTimeoutSeconds);
    }

    /**
     * 获取协议
     */
    @Override
    public String getProtocol() {
        return Mysql.class.getSimpleName();
    }

}
