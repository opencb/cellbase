FROM ubuntu:latest

RUN mkdir /code
WORKDIR /code

env DEBIAN_FRONTEND noninteractive
RUN apt-get update && \
    apt-get install -y build-essential python \
    python-dev python-pip

ENV PYTHONUNBUFFERED 1
ADD . /code

RUN pip install -r requirements.txt
