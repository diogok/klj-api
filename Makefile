IMAGE=diogok/klj-api
IMAGE_NATIVE=diogok/klj-api:native


run-local:
	clj -A:run

native-local-docker:
	clj -A:uberjar
	docker build --network host -t $(IMAGE_NATIVE) -f Dockerfile.nativelocal .

run-native-docker: native-local-docker
	docker rm klj-api-native -f || true
	docker run -ti -p 8080:8080 --name klj-api-native $(IMAGE_NATIVE)

stop-native-docker:
	docker stop klj-api-native