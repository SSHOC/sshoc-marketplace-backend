FROM solr:10.0.0

USER root

RUN mkdir -p /usr/solr && \
	chown solr -hR /usr/solr && \
	chown solr -hR /opt/solr-10.0.0 && \
	chown solr -hR /var/solr

USER solr

WORKDIR /opt/solr-10.0.0

RUN /opt/solr/docker/scripts/init-var-solr && \
    ./bin/solr start --user-managed && \
    ./bin/solr create -c marketplace-items && \
    ./bin/solr create -c marketplace-concepts && \
    ./bin/solr create -c marketplace-actors && \
    ./bin/solr stop && \
    cp -R /var/solr/data/marketplace-items /usr/solr && \
	cp -R /var/solr/data/marketplace-concepts /usr/solr && \
	cp -R /var/solr/data/marketplace-actors /usr/solr && \
	rm -r /var/solr/data/marketplace-items && \
	rm -r /var/solr/data/marketplace-concepts && \
	rm -r /var/solr/data/marketplace-actors

WORKDIR /usr/solr

COPY items/conf/managed-schema items/conf/solrconfig.xml ./marketplace-items/conf/
COPY concepts/conf/managed-schema concepts/conf/solrconfig.xml ./marketplace-concepts/conf/
COPY actors/conf/managed-schema actors/conf/solrconfig.xml ./marketplace-actors/conf/

COPY docker-entrypoint.sh ./
ENTRYPOINT [ "./docker-entrypoint.sh", "--user-managed" ]
