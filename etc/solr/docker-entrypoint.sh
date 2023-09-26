#!/bin/bash

SOLR_MARKETPLACE_ITEMS_DIR="/var/solr/data/marketplace-items"
SOLR_MARKETPLACE_CONCEPTS_DIR="/var/solr/data/marketplace-concepts"
SOLR_MARKETPLACE_ACTORS_DIR="/var/solr/data/marketplace-actors"

/opt/solr/docker/scripts/init-var-solr

if [ ! -d $SOLR_MARKETPLACE_ITEMS_DIR ]; then
	cp -R /usr/solr/marketplace-items /var/solr/data
fi

if [ ! -d $SOLR_MARKETPLACE_CONCEPTS_DIR ]; then
	cp -R /usr/solr/marketplace-concepts /var/solr/data
fi

if [ ! -d $SOLR_MARKETPLACE_ACTORS_DIR ]; then
	cp -R /usr/solr/marketplace-actors /var/solr/data
fi

# Execute parent entrypoint script
exec /opt/solr/docker/scripts/docker-entrypoint.sh "$@"
