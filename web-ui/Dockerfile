FROM nginx
LABEL authors="cbq"

# 映射 HTML
COPY dist ./usr/share/nginx/html
# Conf
COPY conf/nginx.conf ./etc/nginx/conf.conf
COPY conf/conf.d ./etc/nginx/conf.d

EXPOSE 5173