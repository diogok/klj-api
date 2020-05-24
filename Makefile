
build-docker:
	docker build -t diogok/klj-api:jvm .

test-docker-a:
	docker run -d --name kapi-jvm -p 8080:8080 diogok/klj-api:jvm
	sleep 5
	curl localhost:8080/hello/me --fail -v && echo PASSED || echo FAILED
	docker logs kapi-jvm

test-docker: clean build-docker test-docker-a 

build-docker-native:
	docker build -f Dockerfile.native -t diogok/klj-api:native .

test-docker-native-a:
	docker run -d --name kapi-native -p 8080:8080 diogok/klj-api:native
	sleep 5
	curl localhost:8080/hello/me --fail -v && echo PASSED || echo FAILED
	docker logs kapi-native

test-docker-native: clean build-docker-native test-docker-native-a

clean:
	docker stop kapi-jvm kapi-native || true
	docker rm kapi-jvm kapi-native || true
