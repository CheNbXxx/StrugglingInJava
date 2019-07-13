package top.chenbxxx.springboot_mooc.conditional;

import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;
import top.chenbxxx.springboot_mooc.annotation.ConditionalOnSystemProperty;

import java.util.Map;
import java.util.Objects;

/**
 * Java系统的条件判断
 *
 * @author chen
 * @date 19-6-23 下午5:20
 */
public class OnSystemPropertyConditional implements Condition {
    /**
     * 具体判断的方法,为true时候加载Bean
     *
     * @param conditionContext      条件上下文
     * @param annotatedTypeMetadata 注解的元数据
     * @return
     */
    @Override
    public boolean matches(ConditionContext conditionContext, AnnotatedTypeMetadata annotatedTypeMetadata) {

        // 获取到name和value的Map
        Map<String, Object> annotationAttributes = annotatedTypeMetadata.getAnnotationAttributes(ConditionalOnSystemProperty.class.getName());

        if (Objects.isNull(annotationAttributes)) {
            return false;
        }

        return String.valueOf(annotationAttributes.get("value")).equals(System.getProperty(String.valueOf(annotationAttributes.get("name"))));
    }
}
