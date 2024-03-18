package vip.logz.rdbsync.common.job.context.impl;

import org.apache.flink.api.connector.source.Source;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import vip.logz.rdbsync.common.config.PipelineSourceProperties;
import vip.logz.rdbsync.common.exception.UnsupportedSourceProtocolException;
import vip.logz.rdbsync.common.job.context.ContextMeta;
import vip.logz.rdbsync.common.job.context.ContextSourceHelper;
import vip.logz.rdbsync.common.job.debezium.DebeziumEvent;
import vip.logz.rdbsync.common.rule.Rdb;
import vip.logz.rdbsync.common.utils.ClassScanner;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

/**
 * 任务上下文来源辅助的代理
 *
 * @author logz
 * @date 2024-01-09
 */
public class ContextSourceHelperProxy implements ContextSourceHelper<Rdb> {

    /** 日志记录器 */
    private static final Logger LOG = LoggerFactory.getLogger(ContextSourceHelperProxy.class);

    /** 原生辅助映射 [protocol -> helper] */
    private final Map<String, ContextSourceHelper<Rdb>> rawHelperMap = new HashMap<>();

    /**
     * 构造器
     */
    @SuppressWarnings({"unchecked"})
    public ContextSourceHelperProxy() {
        // 在超类所在包中深度扫描
        Class<?> superclass = ContextSourceHelper.class;
        for (Class<?> helperClass : ClassScanner.scanByClass(superclass, superclass.getPackageName())) {
            if (helperClass == superclass) {
                continue;
            }

            try {
                // 解析辅助的协议名
                Class<?> rdbType = extractRdbType(helperClass);
                String protocol = rdbType.getSimpleName().toLowerCase();
                // 实例化辅助
                Constructor<?> constructor = helperClass.getDeclaredConstructor();
                ContextSourceHelper<Rdb> loader = (ContextSourceHelper<Rdb>) constructor.newInstance();
                // 记录辅助实例
                rawHelperMap.put(protocol, loader);
            } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException | InstantiationException e) {
                LOG.info("Create " + helperClass + " instance failed.", e);
            }
        }
    }

    /**
     * 提取关系数据库类型
     * @param helperClass 辅助类
     * @return 返回 {@link Rdb} 的实现类
     */
    private static Class<?> extractRdbType(Class<?> helperClass) {
        // 遍历辅助类实现的接口
        for (Type genericInterface : helperClass.getGenericInterfaces()) {
            // 1. 排除非参数化类型
            if (!(genericInterface instanceof ParameterizedType)) {
                continue;
            }

            // 2. 排除原生类型不是ContextDistHelper的接口
            ParameterizedType paramType = (ParameterizedType) genericInterface;
            if (paramType.getRawType() != ContextSourceHelper.class) {
                continue;
            }

            // 3. 获取ContextDistHelper的参数值，即Rdb实现类
            return (Class<?>) paramType.getActualTypeArguments()[0];
        }

        throw new IllegalArgumentException(helperClass + " is not ContextDistHelper implement class.");
    }

    /**
     * 获取数据源
     * @param contextMeta 任务上下文元数据
     */
    @Override
    public Source<DebeziumEvent, ?, ?> getSource(ContextMeta contextMeta) {
        String protocol = contextMeta.getPipelineSourceProperties().get(PipelineSourceProperties.PROTOCOL);
        ContextSourceHelper<Rdb> rawHelper = getRawHelper(protocol);
        return rawHelper.getSource(contextMeta);
    }

    /**
     * 获取原生辅助
     * @param protocol 协议名
     */
    private ContextSourceHelper<Rdb> getRawHelper(String protocol) {
        ContextSourceHelper<Rdb> helper = rawHelperMap.get(protocol.toLowerCase());

        if (helper == null) {
            throw new UnsupportedSourceProtocolException(protocol);
        }
        return helper;
    }

}
