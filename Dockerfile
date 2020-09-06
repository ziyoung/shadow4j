FROM golang:1.14.4-alpine3.12 AS builder

ENV GO111MODULE on
ENV GOPROXY https://goproxy.cn
WORKDIR /test-essential

COPY ./docker-entrypoint.sh .

RUN apk upgrade && \
    apk add git && \
    go get github.com/shadowsocks/go-shadowsocks2 && \
    go get github.com/ziyoung/socks5-dump && \
    cp /go/bin/go-shadowsocks2 . && \
    cp /go/bin/socks5-dump .

FROM alpine:3.12 AS dist

RUN apk upgrade && \
    rm -rf /var/cache/apk/*

COPY --from=builder /test-essential/ /usr/bin

ENTRYPOINT ["sh","/usr/bin/docker-entrypoint.sh"]
