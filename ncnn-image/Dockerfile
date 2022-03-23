FROM node:lts-alpine
LABEL maintainer="ablocker ablocker@163.com"

# RUN sed -i 's/dl-cdn.alpinelinux.org/mirrors.aliyun.com/g' /etc/apk/repositories
# RUN apk update

RUN yarn config set registry https://repo.nju.edu.cn/repository/npm/
ENV NODE_ENV=product

WORKDIR /ncnn-image
COPY ./ /ncnn-image/

RUN yarn
RUN yarn cache clean

ENTRYPOINT [ "yarn", "serve" ]
