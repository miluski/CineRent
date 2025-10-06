FROM node:22.12.0-alpine3.21 AS build

WORKDIR /app

RUN apk update && apk upgrade --no-cache && \
    rm -rf /var/cache/apk/* /tmp/* /var/tmp/*

COPY package*.json ./

RUN npm ci && npm cache clean --force

COPY . .

RUN npm run build

FROM nginxinc/nginx-unprivileged:1.27-alpine

COPY --from=build --chown=nginx:nginx /app/dist /usr/share/nginx/html

EXPOSE 8080

USER nginx

CMD ["nginx", "-g", "daemon off;"]
