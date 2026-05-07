# Dockerfile para POS Backend
FROM eclipse-temurin:21-jre-alpine

# Crear usuario no-root
RUN addgroup -g 1001 -S posgroup && \
    adduser -S posuser -u 1001 -G posgroup

# Crear directorios necesarios
RUN mkdir -p /app /var/log/pos && \
    chown -R posuser:posgroup /app /var/log/pos

# Cambiar a usuario no-root
USER posuser

# Establecer directorio de trabajo
WORKDIR /app

# Copiar el JAR construido
COPY target/pos-backend-*.jar app.jar

# Exponer puerto
EXPOSE 8080

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=60s --retries=3 \
    CMD wget --no-verbose --tries=1 --spider http://localhost:8080/api/v1/actuator/health || exit 1

# Ejecutar aplicación
ENTRYPOINT ["java", \
    "-Djava.security.egd=file:/dev/./urandom", \
    "-Dspring.profiles.active=prod", \
    "-jar", \
    "app.jar"]