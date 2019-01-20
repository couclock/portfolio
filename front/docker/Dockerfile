
FROM nginx:1.15-alpine
EXPOSE 80
COPY dist /usr/share/nginx/html

COPY nginx-default.conf /etc/nginx/conf.d/default.conf

CMD ["nginx", "-g", "daemon off;"]
