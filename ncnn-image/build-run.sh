docker build -t sun/ncnn-image:latest . && \
docker run \
    --name ncnn-image \
    -d \
    -v $(pwd)/public:/ncnn-image/public \
    -p 8082:8080\
    sun/ncnn-image
