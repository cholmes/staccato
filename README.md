<img src="https://github.com/radiantearth/stac-site/raw/master/images/logo/stac-030-long.png" alt="stac-logo" width="700"/>   

[![Build Status](https://travis-ci.org/boundlessgeo/staccato.svg?branch=master)](https://travis-ci.org/boundlessgeo/staccato)
[![Docker Repository on Quay](https://quay.io/repository/boundlessgeo/staccato/status "Docker Repository on Quay")](https://quay.io/repository/boundlessgeo/staccato)
[![Gitter chat](https://badges.gitter.im/USER/REPO.png)](https://gitter.im/SpatioTemporal-Asset-Catalog/Lobby "Gitter chat")

## About

The SpatioTemporal Asset Catalog (STAC) specification aims to standardize the way geospatial assets are exposed online 
and queried. A 'spatiotemporal asset' is any file that represents information about the earth captured in a certain 
space and time. The initial focus is primarily remotely-sensed imagery (from satellites, but also planes, drones, 
balloons, etc), but the core is designed to be extensible to SAR, full motion video, point clouds, hyperspectral, LiDAR 
and derived data like NDVI, Digital Elevation Models, mosaics, etc. 

The goal is for all major providers of imagery and other earth observation data to expose their data as SpatioTemporal 
Asset Catalogs, so that new code doesn't need to be written whenever a new JSON-based REST API comes out that makes its 
data available in a slightly different way. This will enable standard library components in many languages. STAC can 
also be implemented in a completely 'static' manner, enabling data publishers to expose their data by simply publishing 
linked JSON files online.

## WARNING

The specification is currently still an early version, with the potential for some major things to change. The core is 
now fleshed out, so implementors are encouraged to try it out and give feedback. But the goal is to actually be able to 
act on that feedback, which will mean changes are quite possible. 

But efforts will be made to maintain the core fields established in the central 
[Item spec](https://github.com/radiantearth/stac-spec/tree/master/item-spec), 
[Catalog spec](https://github.com/radiantearth/stac-spec/tree/master/catalog-spec) and 
[Collection spec](https://github.com/radiantearth/stac-spec/tree/master/collection-spec). The minimal amount is 
specified right now, but best practices should emerge with implementation and more will likely be specified.

## Requirements

### Building 
Requires:
- maven 3.x

Example build command: `mvn clean install`

Additionally the docker image can be built from the [staccato-main](./staccato-main) package using the command: 
`mvn dockerfile:build`

### Running 
- Requires Java 8.x, Elasticsearch 6.x 

An Elasticsearch instance must be available.  To run locally in a docker container, use:

`docker run -d -p 9200:9200 -p 9300:9300 -e "discovery.roles=single-node" docker.elastic.co/elasticsearch/elasticsearch:6.6.0`

Any of the following methods are acceptable ways of running Staccato
- `./staccato-{version}.jar (self executing jar)`
- `java -jar staccato-{version}.jar`
- `mvn spring-boot:run` (from the [staccato-main](./staccato-main) directory)
- `docker run -d -i -t -p:8080:8080 quay.io/boundlessgeo/staccato:{version}`

## Endpoints

### API Endpoints

- GET /stac/search - dynamic catalog endpoint
- GET /stac/search/{id} - returns an item by ID

### Collection Endpoints

- GET /collection/{collection_id} - retrieves a collection by ID
- GET /collection/{collection_id}/items - retrieves a collection of items belonging to a collection
- GET /collection/{collection_id}/items/{id} - retrieves an item by ID from a collection

### Catalog Endpoints

- GET /stac - retrieves the root catalog
- GET /stac/{catalog_id} - retrieves a catalog by ID
- GET /stac/{catalog_id}/items - retrieves a collection of items belonging to a collection
- GET /stac/{collection_id}/items/{id} - retrieves an item by ID from a collection
             
### Transaction Endpoints

- POST /stac/{collection_id}/items - creates a new item
- PUT /stac/{collection_id}/items/{item_id} - creates a new item
- PATCH /stac/{collection_id}/items/{item_id} - updates an item item
- DELETE /stac/{collection_id}/items/{item_id} - deletes an item
              
### Stats Endpoints

- GET /stac/stats - retrieves aggregations for all collections
- GET /stac/stats/{collection_id} - retrieves aggregations for a specific collection

### Schema Endpoints

- GET /stac/schema - returns the STAC specification in JSON format
- GET /stac/schema/{collection_id} - returns the JSON schema for the specified collection

### Actuator Endpoints

- GET /actuator - returns a list of utility endpoints for the application

### Query Parameters (all optional)
- **limit** the maximum number of items to return, example limit=100
- **next** to paginate, example next=2 (for the second page of results)
- **time** implicit range query, example time=1995-01-01T00:00:2005-01-01T00:00:00
- **bbox** implicit intersects query, example bbox=-180,-90,180,90
- **search** a Common Query Language text string to query properties of the catalog entry (see below for examples)
- **propertyname** a comma separated list of json field names to include in the result

## Configuartion

The STAC API has several properties that are configurable from the command line, as environment properties in the 
[application.yml](./staccato-main/src/main/resources/application.yml) file.  The table below details the properties that 
are available for configuration.

Property | Default Value | Description
---------|---------------|------------
staccato.include-null-fields | false | Determines whether fields with null values should be serialized or excluded
staccato.generate-self-links | true | Determines whether self links are automatically generated for items
staccato.generate-thumbnail-links | true | Determines whether thumbnail links are automatically generated for items
staccato.async-bridge-thread-pool.max-threads | 200 | The size of the threadpool to be used for blocking async requests using the Elasticsearch REST client
staccato.async-bridge-thread-pool.daemon | true | false if the Scheduler requires an explicit Scheduler.dispose() to exit the VM
staccato.es.scheme | http | The scheme to be used for connection to Elasticsearch
staccato.es.host | localhost | The hostname of the Elasticsearch aggregationService
staccato.es.port | 9200 | The Elasticsearch aggregationService port
staccato.es.number-of-shards | 5 | The number of shards used when auto-initializing an Elasticsearch index
staccato.es.number-of-replicas | 0 | The number of replicas used when auto-initializing an Elasticsearch index
staccato.es.type | _doc | The Elasticsearch document type.  It is not recommended to change this from it's default value as "_doc" will be the only value supported in ES7
staccato.es.max-reconnection-attempts | 10 | The number of reconnection attempts to the Elasticsearch aggregationService
staccato.es.rest-client-max-connections-total | 200 | The Elasticsearch client threadpool size.  This is the maximum number of connections a single STAC instance may have open to Elasticsearch.
staccato.es.rest-client-max-connections-per-route | 200 | The maximum number of Elasticsearch client connections per route.
staccato.es.rest-client-max-retry-timeout-millis | 60000 | The Elasticsearch client timeout value in milliseconds.
staccato.links.self.scheme | http | The scheme to be used when building self links for items
staccato.links.self.host | localhost | The host to be used when building self links for items
staccato.links.self.port | 8080 | The port to be used when building self links for items
staccato.links.self.context-path | / | The context path to be used when building self links for items
staccato.links.thumbnails.scheme | http | The scheme to be used when building thumbnail links for items
staccato.links.thumbnails.host | localhost | The host to be used when building thumbnail links for items
staccato.links.thumbnails.port | 8080 | The port to be used when building thumbnail links for items
staccato.links.thumbnails.context-path | / | The context path to be used when building thumbnail links for items
staccato.kafka.enabled | false | Setting value to true enables the kafka listener for adding items to the catalog
staccato.kafka.bootstrap-servers | localhost:9092 | A list of Kafka bootstrap servers
staccato.kafka.group-id-config | stac-group | The Kafka group ID
staccato.kafka.client-id-config | stac-consumer | the Kafka client ID
staccato.kafka.auto-offset-reset-config | earliest | Used to set the start offset to the earliest or latest offset on the partition
staccato.kafka.topic | stac | The Kafka topic to listen on
staccato.grpc.port | 9999 | The listening port for incoming gRPC requests
staccato.rsocket.port | 7000 | The listening port for incoming RSocket requests

Additionally, Spring framework uses configuration properties for its configuration.  While not exhaustive, Spring 
offers a list of 
<a href="https://docs.spring.io/spring-boot/docs/current/reference/html/common-application-properties.html">commonly 
used configuration properties</a>.

Passing in custom properties depends on how you are running STAC.  Below are examples using java and maven from the 
command line:

Set the active profile:
 * java: <code>java -jar -Dstac.es.host=127.0.0.1 stac.jar</code>
 * maven: <code>mvn spring-boot:run -Dstac.es.host=127.0.0.1</code>
 
Set the server port:
 * java: <code>java -jar -Dserver.port=8081 stac.jar</code>
 * maven: <code>mvn spring-boot:run -Dserver.port=8081</code>
 
In addition, properties can be set via environment variables.  The variable names should follow the following rules:
 * Strictly use all uppercase
 * Replace all periods in the property path with underscores
 * Separate camelcase variables with underscore where the case changes

Example:

| STAC Property Name | Environment Variable Name |
|------------------|---------------------------|
| test | TEST |
| server.port | SERVER_PORT |
| staccato.kafka.enabled | STACCATO_KAFKA_ENABLED |
| staccato.kafka.bootstrap-servers | STACCATO_KAFKA_BOOTSTRAP_SERVERS |

## Code

### Spring Boot / WebFlux

Staccato is built using the latest versions of 
<a href="https://docs.spring.io/spring-boot/docs/current/reference/htmlsingle/">Spring Boot</a> and 
<a href="https://docs.spring.io/spring/docs/current/spring-framework-reference/web-reactive.html">Spring WebFlux</a>. The 
codebase is written reactively, utilizing the <a href="https://projectreactor.io/">Project Reactor</a> library.

### Filters

Staccato implements a concept called filters, which allows items to be modified or transformed during any/all of 3 
different operations:

* [index](./staccato-commons/src/main/java/com/planet/staccato/filter/ItemIndexFilter.java)
* [update](./staccato-commons/src/main/java/com/planet/staccato/filter/ItemUpdateFilter.java)
* [search](./staccato-commons/src/main/java/com/planet/stacccato/filter/ItemSearchFilter.java)

Any Spring managed bean that implements one of these interfaces will be called during the corresponding event in the 
request lifecycle.  An bean that implements ItemIndexFilter will be called before an item is indexed in Elasticsearch.  
The update query will be called before an item is updated in Elasticsearch.  The search query will be called *after* an 
item is retrieved from Elasticsearch.

Each query interface defines a method to return the list of item types that the query should be applied to, along with 
the actual `doFilter` method which does the actual work.  The basic premise is that the `doFilter` method accepts an 
Item as input and returns an item as output.  This can be used to automatically add data, remove data, or transform 
data.  Several examples of some included filters can be found in the 
[filter](./staccato-main/src/main/java/com/planet/staccato/filter) package.  Collections can also provide custom  
filters to accomplish various tasks, such as automatically generating links to related items based on values found in 
the item's properties.

## Extensions and Collections

### Overview

The STAC item spec only has one requirement for item properties: to provide a `datetime` field. Properties specific to
certain datasets or product types will be developed by the community as extensions and move through a series of maturity 
steps as outlined [here](https://github.com/radiantearth/stac-spec/tree/master/extensions). This STAC implementation was
originally designed for internal use at Boundless Spatial and was intended to only offer only a small number of static
collections. As such, it is not currently capable of providing a way to dynamically add or define collections. Adding
such a capability may be a good idea for the future.

For each extension that has currently been proposed, the `properties` fields defined by the extension are described in 
interfaces in the [commons extension package](/stac-api/staccato-commons/src/main/java/com/planet/staccato/extension).
The extensions are defined as interfaces so that a mix of multiple extensions can be combined to create a set of
heterogeneous properties for a collection. 

### Creating a new collection

Collections are currently defined in the [staccato-collections](./staccato-collections) module. When defining a new 
collection, you'll typically want to create at least 4 Java classes and one Spring auto-configuration file:

1) If you need to define more properties for you collection than are defined by the community in the
[commons extension package](./staccato-commons/src/main/java/com/planet/staccato/extension), you'll need to
create an interface that defines all the getters and setters for your model, along with Jackson annotations to make sure
the data is serialized/deserialized the way you want.
2) An implementation of your model. This implementation _MUST_ also implement
[`MandatoryProperties`](./staccato-commons/src/main/java/com/planet/staccato/model/MandatoryProperties.java)
3) An implementation of 
[`CollectionMetadata`](./staccato-commons/src/main/java/com/planet/staccato/collection/CollectionMetadata.java)
or simply extend 
[`CollectionMetadataAdapter`](./stac-api/staccato-commons/src/main/java/com/planet/staccato/collection/CollectionMetadataAdapter.java).
4) A class annotated with `@Configuration` that creates 2 beans, both instances of your `CollectionMetadata` class.
One bean is the the WFS3 collection and one is the STAC catalog. Yes, it seems silly, but there are differences per
the spec (the collection is WFS3 compliant; the catalog enables STAC-specific capabilities, such as the traversing
subcatalogs). It is important that when creating the collection bean, you set 
`metadata.setCatalogType(CatalogType.COLLECTION);` and when you create the catalog bean, you set 
`metadata.setCatalogType(CatalogType.CATALOG);`.
5) A [spring.factories](./staccato-collections/landsat8/src/main/resources/META-INF/spring.factories) file in 
`/src/main/resources/META-INF` that points to your `@Configuration` class. This tells any Spring Boot application that 
uses this module as a dependency where to find the auto configuration class, even if component scanning isn't configured 
to scan your extension package path. 

Notes on the `CollectionMetadata` class: The `properties` section in the collection endpoint can contain fields/values
that are shared amongst all items in your collection to avoid duplicating the data in every single item.

It is also important to note that this implementation currently relies on implementing the commons extension to provide
the `collection` field in every item. Because each collection will have a different properties implementation that may
implement several different extension interfaces or custom fields, Jackson cannot deserialize Item classes without
more information on which properties class to deserialize to. Having the "collections" field in each item provides an
extremely convenient 1:1 relationship between the item and it's properties implementation.  The Jackson configuration 
for this can be found [here](./staccato-main/src/main/java/com/planet/staccato/config/ExtensionConfig.java). 
Also, the [Item class'](./staccato-commons/src/main/java/com/planet/staccato/model/Item.java) primary generic 
properties type is 
[CommonsCollection](./staccato-commons/src/main/java/com/planet/staccato/extension/CommonsCollection.java) so that 
various bits of code, eg the 
[FilterProcessor](./staccato-commons/src/main/java/com/planet/staccato/filter/ItemsFilterProcessor.java)
can easily determine what collection an item belongs to.

### Custom annotations

Staccato currently provides two custom annotations:
- [@Mapping](./staccato-commons/src/main/java/com/planet/staccato/elasticsearch/annotation/Mapping.java)
- [@Subcatalog](./staccato-commons/src/main/java/com/planet/staccato/collection/Subcatalog.java)

The `@Mapping` annotation allows you to define Elasticsearch mapping types that will be applied during 
[automatic index creation](./staccato-elasticsearch/src/main/java/com/planet/staccato/es/initializer/ElasticsearchIndexInitializer.java). 
Set type `type` attribute to one of the enumerated values found in 
[MappingType](./staccato-commons/src/main/java/com/planet/staccato/elasticsearch/annotation/MappingType.java). 

The `@Subcatalog` annotation, when applied to a `getter` interface method, will make that field eligible to be 
automatically subcataloged via the `/stac/{catalog}` endpoint.  The 
[catalog spec implementation](./staccato-main/src/main/java/com/planet/staccato/catalog) will automatically detect 
methods with this annotation and build a subcatalog link containing the field name.  That subcatalog will build links 
containing all unique values in Elasticsearch for that field.  After all eligible subcatalog fields have been 
traversed, the links section will be populated with links to all items that match the selected subcatalog values.


## Elasticsearch

### Automatic Initialization

*NOTE THAT THIS CAPABILITY IS FOR DEMONSTRATION AND TESTING PURPOSES ONLY AND SHOULD NOT BE USED IN PRODUCTION.*

STAC can automatically detect all defined collections and create initial Elasticsearch indexes and basic mappings so 
that no manual configuration of Elasticsearch (besides the actual endpoint) is needed. 

Configure the Elasticsearch endpoint in [application.yml](./stac-main/src/main/resources/application.yml) 
 or using the environment variable equivalents using the following properties:
 
 * `staccato.es.scheme`
 * `staccato.es.host`
 * `staccato.es.port`
 * `staccato.es.user` (optional)
 * `staccato.es.password` (optional)
 
 The automatic initializer _will_ create a template containing a basic matching pattern, a read alias, and mappings, 
 along with the initial index and write alias.  This is a bit more robust of a configuration than simply creating a 
 single index per collection and follows the pattern described below for production environments.

### Production Environments

There are several considerations to that must be taken into account when configuring Elasticsearch for a production 
environment.  It is important to note the following limitations and recommendations for Elasticsearch:

* [Mappings](https://www.elastic.co/guide/en/elasticsearch/reference/current/mapping-types.html) are immutable.  Once 
you define a mapping for a field, it cannot be changed.  It is vital that you understand the various types of available 
mappings and carefully choose which mapping types to use for each field.
* The number of shards in an index cannot be changed.  Elasticsearch recommends not exceeding a shard size of 50GB, both 
for performance reasons and for the ability to easily move shards around if necessary. 
* When the shards of an index exceed the recommended size, it may be convenient to use the 
[rollover API](https://www.elastic.co/guide/en/elasticsearch/reference/master/indices-rollover-index.html).  If the name 
of the index ends in a number, the rollover API can automatically name the new indexes. By default, Elasticsearch will 
use a zero-padded number with a length of 6, so it may be wise to create all initial indices with the suffix `-000001`. 

For a production environment, it is strongly recommended to configure Elasticsearch with the anticipation of using the 
rollover API.  This helps future proof the configuration and provides you with options in the future if you find that 
your shard sizes have exceeded the recommended limit.  A good plan for a production environment is as follows:

1) Never read or write directly to an index.  Put all indices behind aliases.  This allows you to easily reindex or 
point your alias to a different index with no disruption to the service.
2) For a given collection type, determine the desired number of shards, number of read replicas, index naming convention, 
and mappings for all properties.  Using these values, create a 
[template](https://www.elastic.co/guide/en/elasticsearch/reference/current/indices-templates.html). There are 3
important things to consider when creating a template.
   1) Use the index pattern `my-index-name-*`. This means any index ever created that starts
   with `my-index-name-` will have this template with these mappings applied to it and will allow for rolling indices
   should the need ever arise.
   2) STAC should not talk directly to the index.  Two aliases are actually required, the
   search alias and the write alias. you _MUST_ use the pattern `my-alias-name-search` (important part being the
   `-search` at the end). All indices created with this template pattern will automatically be added to this
   alias group.
   3) Mappings - this will create all the of the mappings required for this index.

    Example curl command:

    `curl -X PUT -H "Content-Type: application/json" -T my-template.json http://localhost:9200/_template/my-template`

3) Create the [initial index, along with the write alias](https://www.elastic.co/guide/en/elasticsearch/reference/current/indices-create-index.html#create-index-aliases)
The actual index name should be named `my-index-name-000001`. That is, your index name, followed by a hyphen, followed by
five zeros, followed by the number 1. The write alias is the same value used for the search alias in the template,
minus the `-search` suffix.

    Example curl command:

    `curl -X PUT -H "Content-Type: application/json" -T my-name-index.json http://localhost:9200/my-index-name-000001`

4) When it's all said and done, you should be able to:
   1) Verify the template is created: `http://localhost:9200/_template/my-template`
   2) Verify the index is created: `http://localhost:9200/_cat/indices?v`
   3) Verify the aliases for the index: `http://localhost:9200/_aliases`
   4) Verify the mappings have been created for the index: `http://localhost:9200/my-index-name-000001/`
   
At this stage, you will have a read alias of `my-index-name-search` and a write alias of `my-index-name`.  Both of these 
will point to the actual index of `my-index-name-000001`.  A cron job can be created to continuously poll the rollover
API on some interval.  The request sent to the rollover API  will contain the conditions that will need to be met for 
Elasticsearch to rollover the index.  When the criteria has been met, Elasticsearch will automatically create a new 
index named `my-index-name-000002`.  Because this name matches the pattern `my-index-name-*` that was established in 
our template, all of the shard, read replica, mapping, etc configuration will automatically be applied.  In addition, 
the `my-index-name` write alias will automatically be changed to point to `my-index-name-000002`, and the search alias  
`my-index-name-search` will add to it's list.  `my-index-name-000002`.  When executing searches against the search 
alias `my=index-name-search`, Elasticsearch will return matches from both indexes, `my-index-name-000001` and 
`my-index-name-000002`.  The one important note: if a record needs to be updated, you need to first determine which 
actual index it belongs to and update it on that index.

STAC will need to be configured with the mappings between the Elasticsearch alias name and the collection ID (eg, the
value used in the `items.properties.collection` field). This can be set in 
[application.yml](./staccato-main/src/main/resources/application.yml) under the path 
`stac.es.index.aliases`. The key should be the name of the write alias used in Elasticsearch (not the actual index 
name!). The value should be the collection id.  So in our example case, the key would be `my-index-name` and the value 
would be the collection ID.  STAC will automatically append `-search` to the alias for executing searches.

At this point, you should be good to start inserting items. See the 
[transaction API controller](./staccato-main/src/main/java/com/planet/staccato/transaction/TransactionApi.java)
for the proper methods to use for creating new items.
