#!/usr/bin/env bash
set -e

if [ "$1" == "go-shadowsocks2" -o "$1" == "socks5-dump" ]; then
    exec "$@"
else
  echo "unknown command: $@"
fi
