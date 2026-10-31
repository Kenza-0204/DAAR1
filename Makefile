.PHONY: all build test clean

all: build

build:
	./mvnw clean package

test:
	./mvnw test

clean:
	./mvnw clean
