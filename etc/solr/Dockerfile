FROM solr:8.2.0

USER root
RUN mkdir -p /usr/solr && \
	chown solr -hR /usr/solr && \
	chown solr -hR /opt/solr-8.2.0 && \
	chown solr -hR /var/solr

USER solr

WORKDIR /opt/solr-8.2.0

RUN ./bin/solr start && \
	./bin/solr create_core -c marketplace_items && \
	./bin/solr stop && \
	cp -r /var/solr/data/marketplace_items /usr/solr 

WORKDIR /usr/solr

COPY items/conf/managed-schema items/conf/solrconfig.xml ./marketplace_items/conf/

COPY docker-entrypoint.sh ./
ENTRYPOINT [ "./docker-entrypoint.sh", "solr-foreground" ]
