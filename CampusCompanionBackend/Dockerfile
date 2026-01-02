# 多阶段构建后端 Spring Boot JAR 并运行
FROM maven:3.9.9-eclipse-temurin-21 AS build
WORKDIR /app

# 只复制 pom.xml 先做依赖缓存
COPY pom.xml ./
RUN mvn -B -q dependency:go-offline

# 再复制源码进行打包
COPY src ./src
RUN mvn -B -q clean package -DskipTests

# 运行阶段镜像
FROM eclipse-temurin:21-jre
WORKDIR /app

# 从构建阶段复制打包好的 JAR
COPY --from=build /app/target/CampusCompanionBackend-0.0.1-SNAPSHOT.jar app.jar

# 默认端口
EXPOSE 8080

# 允许通过 JAVA_OPTS 追加 JVM 参数
ENV JAVA_OPTS=""

ENTRYPOINT ["sh","-c","java $JAVA_OPTS -jar /app/app.jar"]
