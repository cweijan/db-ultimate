package github.cweijan.ultimate.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author cweijan
 * @version 2019/8/7 14:52
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface OneToMany {
    /**
     *
     * @return 要关联的表实体Class,默认关联实体的主键
     */
    Class<?> relationClass();

    /**
     *
     * @return 关联表的外键名称
     */
    String relationColumn();

    /**
     * @return 其他条件Sql
     */
    String where() default "";

}
