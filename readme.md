# 项目运行说明

运行环境：Java 8

1. 找到`src/main/resources/application.yml`，修改你的数据库连接配置
    ```
    datasource:
        url: jdbc:mysql://localhost:3306/demo_db?useSSL=false&characterEncoding=utf8&zeroDateTimeBehavior=CONVERT_To_NULL&serverTimezone=Asia/Shanghai
        username: root      #修改你的用户名和密码
        password: password  
        driver-class-name: com.mysql.cj.jdbc.Driver
    ```
   
2. 在MySQl中新建名为`demo_db`的数据库，在该数据库中执行`demo_db.sql`文件建表
3. 在idea中构建并运行该Spring项目
4. 访问`localhost:8888`