package com.refactify.utils

import org.elasticsearch.action.admin.indices.create.CreateIndexRequestBuilder
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest
import org.elasticsearch.action.bulk.BulkRequestBuilder
import org.elasticsearch.action.bulk.BulkResponse
import org.elasticsearch.client.Client
import org.elasticsearch.client.transport.TransportClient
import org.elasticsearch.common.settings.Settings
import org.elasticsearch.common.transport.InetSocketTransportAddress
import org.elasticsearch.node.Node
import org.elasticsearch.node.NodeBuilder

import javax.json.Json
import javax.json.JsonArray
import javax.json.JsonObject
import java.nio.file.*
import java.nio.file.attribute.BasicFileAttributes

class ElasticsearchInstance {
    Node node
    Client client
    Set<String> indicesCreated

    ElasticsearchInstance() {
        String esHome = System.getProperty("es.home.path")
        Settings settings = Settings.settingsBuilder()
                .put("path.home", esHome)
                .put("path.conf", esHome)
                .put("path.data", esHome)
                .put("path.work", esHome)
                .put("path.logs", esHome)
                .put("network.host", "localhost")
                .put("http.port", "9206")
                .put("transport.tcp.port", "9306")
                .put("discovery.zen.ping.multicast.enabled", "false")
                .build()
        node = NodeBuilder.nodeBuilder()
                .settings(settings)
                .local(false)
                .clusterName("es-test")
                .node()
        Settings transportSettings = Settings.settingsBuilder().
                put("cluster.name", "es-test")
                .build()
        client = TransportClient.builder()
                .settings(transportSettings)
                .build()
        client.addTransportAddress(new InetSocketTransportAddress(InetAddress.getByName("localhost"), 9306))
        indicesCreated = [] as Set
    }

    void createIndex(final String indexName, final String type, Path mappingPath) {
        JsonObject mapping = Json.createReader(Files.newInputStream(mappingPath, StandardOpenOption.READ)).readObject()
        Settings indexSettings = Settings.settingsBuilder()
                .put("number_of_shards", 1)
                .put("number_of_replicas", 0)
                .build()
        CreateIndexRequestBuilder indexRequestBuilder = client.admin().indices().prepareCreate(indexName)
                .setSettings(indexSettings)
                .addMapping(type, mapping.getJsonObject("mappings").getJsonObject(type).toString())
        indexRequestBuilder.execute().actionGet()
        client.admin().indices().prepareRefresh(indexName).execute().actionGet()
        indicesCreated.add(indexName)
    }

    void seedTestData(final String index, final String type, final Path dataPath) {
        BulkRequestBuilder bulkRequest = client.prepareBulk()
        JsonArray documents = Json.createReader(Files.newInputStream(dataPath, StandardOpenOption.READ)).readArray()
        for (int i = 0; i < documents.size(); i++) {
            JsonObject document = documents.getJsonObject(i)
            String id = document.getString("_id")
            String source = document.getJsonObject("_source").toString()
            bulkRequest.add(client.prepareIndex(index, type, id).setSource(source))
        }

        BulkResponse bulkResponse = bulkRequest.execute().actionGet()
        if (bulkResponse.hasFailures()) {
            throw new RuntimeException(bulkResponse.buildFailureMessage())
        }
        client.admin().indices().prepareRefresh(index).execute().actionGet()
    }

    void cleanup() {
        for (String index : indicesCreated) {
            DeleteIndexRequest indexRequest = new DeleteIndexRequest(index)
            client.admin().indices().delete(indexRequest).actionGet()
        }
        client.close()
        node.close()
        if (Files.exists(Paths.get(System.getProperty("es.home.path")))) {
            Files.walkFileTree(Paths.get(System.getProperty("es.home.path")), new SimpleFileVisitor<Path>() {
                @Override
                FileVisitResult visitFile(final Path file, final BasicFileAttributes attrs) throws IOException {
                    Files.delete(file)
                    return FileVisitResult.CONTINUE
                }

                @Override
                FileVisitResult postVisitDirectory(final Path dir, final IOException exc) throws IOException {
                    Files.delete(dir)
                    return FileVisitResult.CONTINUE
                }
            })
        }
    }

    Client getClient() {
        return client
    }
}
