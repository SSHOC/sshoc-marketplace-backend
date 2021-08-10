FROM solr:8.2.0

USER root
RUN mkdir -p /usr/solr && \
	chown solr -hR /usr/solr && \
	chown solr -hR /opt/solr-8.2.0 && \
	chown solr -hR /var/solr

USER solr

WORKDIR /opt/solr-8.2.0

RUN ./bin/solr start && \
	./bin/solr create_core -c marketplace-items && \
	./bin/solr create_core -c marketplace-concepts && \
	./bin/solr create_core -c marketplace-actors && \
	./bin/solr stop && \
	cp -r /var/solr/data/marketplace-items /usr/solr && \
	cp -r /var/solr/data/marketplace-actors /usr/solr && \
	cp -r /var/solr/data/marketplace-concepts /usr/solr

WORKDIR /usr/solr

COPY items/conf/managed-schema items/conf/solrconfig.xml ./marketplace-items/conf/
COPY concepts/conf/managed-schema concepts/conf/solrconfig.xml ./marketplace-concepts/conf/
COPY actors/conf/managed-schema actors/conf/solrconfig.xml ./marketplace-actors/conf/

COPY docker-entrypoint.sh ./
ENTRYPOINT [ "./docker-entrypoint.sh", "solr-foreground" ]
