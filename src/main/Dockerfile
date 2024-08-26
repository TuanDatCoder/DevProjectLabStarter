# Step 1: Sử dụng Maven và JDK 21 làm base image cho việc build
FROM maven:3.9.4-eclipse-temurin-21 AS builder

# Step 2: Đặt thư mục làm việc trong container
WORKDIR /app

# Step 3: Copy file pom.xml và các dependencies để cache chúng
COPY pom.xml ./
COPY src/main/resources/application.properties ./src/main/resources/

# Step 4: Cài đặt các dependencies của Maven
RUN mvn dependency:go-offline -B

# Step 5: Copy toàn bộ source code vào container
COPY src ./src

# Step 6: Build ứng dụng
RUN mvn clean package -DskipTests

# Step 7: Sử dụng JDK 21 làm base image để chạy ứng dụng
FROM eclipse-temurin:21-jdk

# Step 8: Copy file JAR từ builder image
COPY --from=builder /app/target/*.jar app.jar

# Step 9: Expose port 8080 và chạy ứng dụng
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
