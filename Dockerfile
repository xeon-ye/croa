FROM java:8
# 作者
MAINTAINER qinfei <hzcl.sky@gmail.com>
RUN ln -sf /usr/share/zoneinfo/Asia/Shanghai /etc/localtime
#RUN echo -e "https://mirror.tuna.tsinghua.edu.cn/alpine/v3.4/main\n\https://mirror.tuna.tsinghua.edu.cn/alpine/v3.4/community" > /etc/apk/repositories
#RUN apk --update add curl bash ttf-dejavu && rm -rf /var/cache/apk/*

# VOLUME 指定了临时文件目录为/tmp。
# 其效果是在主机 /var/lib/docker 目录下创建了一个临时文件，并链接到容器的/tmp
VOLUME /tmp
COPY ./qferp.jar /app/app.jar
COPY ./lib /app/lib
COPY ./fonts /usr/share/fonts/
RUN chmod -R 755 /usr/share/fonts/ \
#    && ttmkfdir -e /usr/share/X11/fonts/encodings/encodings.dir \
    && fc-cache
EXPOSE  810
ENTRYPOINT ["java","-Djava.security.egd=file:/dev/./urandom","-jar","-Dspring.profiles.active=docker","/app/app.jar","> /qferp/logs/810.log"]