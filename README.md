# 1.前置说明
> 官方文档：http://xbatis.cn
## 1.1 什么是 xbatis
> xbatis 是一个基于mybatis的轻量级ORM框架；拥有流式丰富的API；设计超前，理念超前，稳定可靠！

> 支持spring,springboot;也支持solon
> 
## 1.2 和mybatis的关系
> xbatis 是基于mybatis扩展的ORM框架，因为mybatis原则上不算是ORM框架；xbatis是补充、扩展！
## 1.3 xbatis的优势
>1. 轻量级封装mybatis,对mybatis的代码改动微乎其微；所以完全兼容mybatis所有功能，包括配置和使用

>2. 基于注解，映射数据库；

>3. 支持多表 join 和返回；

>4. api 采用 lambda 和 stream 流式设计；

>5. select 自动化以及结果 1 对 1,1 对多，超级方便快捷；

>6. 内置分页以及超强的 sql 优化功能；

>7. 稳定且性能极优；

>8. api 丰富，支持数据库函数、多表、乐观锁、多租户、逻辑删除、默认值（可动态值） 等众多功能；

>9. 零学习成本，和写 sql 一样方便。

>10. 支持复杂的返回关系映射：例如一对一 ，一对多，多对多；

>11. 支持自定义 sql 模板，再也不用担心框架支持不足；

>12. 内置代码生成器，通过配置，可定制自身规范；

# 2. 如何在solon中使用
## 2.1 maven 引入
```
<dependency>
    <groupId>cn.xbatis</groupId>
    <artifactId>xbatis-solon-plugin</artifactId>
    <version>1.9.2-RC5</version>
</dependency>

<dependency>
    <groupId>org.noear</groupId>
    <artifactId>mybatis-solon-plugin</artifactId>
    <version>3.5.0</version>
</dependency>
```
>xbatis-solon-plugin 是 xbatis针对solon 提供的插件

## 2.2 solon yaml 配置
```yaml
# 配置数据源
ds:
  # 与数据库名可用保持一致
  schema: rock
  jdbcUrl: jdbc:mysql://localhost:3306/test?useUnicode=true&characterEncoding=utf8&autoReconnect=true&rewriteBatchedStatements=true
  driverClassName: com.mysql.cj.jdbc.Driver
  username: root
  password: 123456

# 规则 mybatis.数据源名称;数据源名称 是 数据源 Bean的名称
mybatis.master:
  mappers:       #支持包名 或 类名（大写开头 或 *）或 xml（.xml结尾）；支持目录 ** 或 * 占位符
    - "test.UserMapper"
    - "demo4021.**.mapper"
    - "demo4021.**.mapper.*" #这个表达式同上效果
    - "classpath:demo4021/**/mapper.xml"
    - "classpath:demo4021/**/mapping/*.xml"   

```
## 2.3 DataSource Bean配置（数据源配置和xbatis关系不大，参看[solon官网的数据源配置](https://solon.noear.org/article/794)即可）
```java
@Configuration
public class MybatisConfig {

    @Bean(name = "master", typed = true)
    public DataSource dataSource(@Inject("${ds}") HikariDataSource ds) {
        return ds;
    }


//    @Bean(name = "db1", typed = true)
//    public DataSource db1(@Inject("${ds.db1}") HikariDataSource ds) {
//        return ds;
//    }
//
//    @Bean(name = "db2", typed = true)
//    public DataSource db2(@Inject("${ds.db1}") HikariDataSource ds) {
//        return ds;
//    }
}
```
## 2.4 在业务中使用

```java
import org.noear.solon.annotation.Get;

// Mapper 需要继承 MybatisMapper
public interface UserMapper extends MybatisMapper<User> {
}

// 在 Controller 中使用 （这是示例，不建议在Controller 里使用）
@Controller
public class DemoController {

    @Db  //因为只有一个数据源 所以可简写；如果多个，则：@Db("master")
    UserMapper mapper;
    
    @Get
    @Mapping("/test")
    public void test() {
        List<User> userList = QueryChain.of(mapper)
                .eq(User::getId)
                .like(User::getName,"abc")
                .list();
        userList.forEach(item -> System.out.println(item.getName()));
    }
}
```

# 3.solon 容器下的启动时安全检查配置(yml配置,开发环境推荐必填)
```yaml
mybatis.db1:
    pojoCheck:
      basePackages: com.example.**.po
    mappers:
    - "com.**.mapper.TestMapper"
```
> pojoCheck 下配置 basePackages、modelPackages、resultEntityPackages、conditionTargetPackages、orderByTargetPackages

## 3.1 pojoCheck下属性说明

| 属性名                     | 说明                                    |
|-------------------------|---------------------------------------|
| basePackages            | 基础包路径                                 |
| modelPackages           | Model类的包扫描路径；如果没填，则使用basePackages的路径  |
| resultEntityPackages    | VO类的包扫描路径；如果没填，则使用basePackages的路径     |
| conditionTargetPackages | 对象转条件的类的包扫描路径；如果没填，则使用basePackages的路径 |
| orderByTargetPackages   | 对象转排序的类的包扫描路径；如果没填，则使用basePackages的路径 |

# 4.更多使用xbatis的使用说明
请前往xbatis 查看：https://gitee.com/xbatis

# 5.更多配置说明
配置和 solon mybatis-solon-plugin 配置是一样的：https://solon.noear.org/article/20

 

