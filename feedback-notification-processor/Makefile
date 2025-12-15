build-NotificationProcessorFunction:
	mvn package -Pnative -Dquarkus.native.native-image-xmx=4g -Dfile.encoding=UTF-8
	cp target/function.zip $(ARTIFACTS_DIR)