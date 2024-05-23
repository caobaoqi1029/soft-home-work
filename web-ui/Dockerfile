FROM nginx
LABEL authors="cbq"

COPY dist ./usr/share/nginx/html
COPY conf/nginx.conf ./etc/nginx/conf.conf
COPY conf/conf.d ./etc/nginx/conf.d

EXPOSE 80
