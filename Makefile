.PHONY: db-changelog-diff

db-changelog-diff:
	mkdir -p target/generated/liquibase
	mvn liquibase:generateChangeLog
