# Dockerfile para POS Frontend (Multi-stage build)
FROM node:20-alpine as builder

# Crear usuario no-root
RUN addgroup -g 1001 -S posgroup && \
    adduser -S posuser -u 1001 -G posgroup

# Cambiar a usuario no-root
USER posuser

# Establecer directorio de trabajo
WORKDIR /app

# Copiar archivos de configuración
COPY package*.json ./

# Instalar dependencias
RUN npm ci --only=production

# Copiar código fuente
COPY . .

# Build de producción
RUN npm run build

# Etapa de producción
FROM nginx:alpine

# Copiar configuración nginx personalizada
COPY nginx.conf /etc/nginx/nginx.conf

# Copiar archivos construidos
COPY --from=builder /app/dist /usr/share/nginx/html

# Crear usuario no-root para nginx
RUN addgroup -g 1001 -S posgroup && \
    adduser -S posuser -u 1001 -G posgroup && \
    chown -R posuser:posgroup /usr/share/nginx/html /var/cache/nginx /var/log/nginx /etc/nginx

# Cambiar a usuario no-root
USER posuser

# Exponer puerto
EXPOSE 80

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=5s --retries=3 \
    CMD wget --no-verbose --tries=1 --spider http://localhost/ || exit 1

# Comando por defecto
CMD ["nginx", "-g", "daemon off;"]