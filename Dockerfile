FROM pandeiro/lein
MAINTAINER Aliaksandr Kazlou <aliaksandr.kazlou@gmail.com>

COPY . /app

EXPOSE 3000

CMD ["ring", "server-headless"]
