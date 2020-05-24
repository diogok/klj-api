
build-docker:
	docker build -t diogok/klj-api:jvm .

test-docker-a:
	docker run -d --name kapi-jvm -p 8080:8080 diogok/klj-api:jvm
	sleep 5
	curl localhost:8080/hello/me --fail -v && echo PASSED || echo FAILED
	docker logs kapi-jvm

clean-docker:
	docker stop kapi-jvm  || true
	docker rm kapi-jvm || true

test-docker: clean-docker build-docker test-docker-a clean-docker

build-docker-native:
	docker build -f Dockerfile.native -t diogok/klj-api:native .

test-docker-native-a:
	docker run -d --name kapi-native -p 8080:8080 diogok/klj-api:native
	sleep 5
	curl localhost:8080/hello/me --fail -v && echo PASSED || echo FAILED
	docker logs kapi-native

clean-docker-native:
	docker stop kapi-native  || true
	docker rm kapi-native || true

test-docker-native: clean-docker-native build-docker-native test-docker-native-a clean-docker-native

clean:
	docker stop -f kapi-jvm kapi-native
	docker rm -f kapi-jvm kapi-native
