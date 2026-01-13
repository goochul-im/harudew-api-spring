package b1a4.harudew.global.config

import io.qdrant.client.QdrantClient
import io.qdrant.client.QdrantGrpcClient
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class QdrantConfig(
    @Value("\${qdrant.host}") private val host: String,
    @Value("\${qdrant.port}") private val port: Int
) {

    @Bean
    fun qdrantClient(): QdrantClient {
        return QdrantClient(QdrantGrpcClient.newBuilder(host, port, false).build())
    }

}
