JAVA_HOME := $(shell /usr/libexec/java_home -v 17 2>/dev/null || echo $(JAVA_HOME))
MVN := JAVA_HOME="$(JAVA_HOME)" mvn

.PHONY: test run docker-up docker-down package

docker-up:
	docker compose up -d

docker-down:
	docker compose down

test:
	$(MVN) test

package:
	$(MVN) -DskipTests package

run: docker-up
	$(MVN) spring-boot:run
