import com.hmall.common.util.FileUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpHost;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.indices.CreateIndexRequest;
import org.elasticsearch.client.indices.GetIndexRequest;
import org.elasticsearch.common.xcontent.XContentType;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;

/**
 * ClassName IndexTest
 *
 * @author qml
 * Date 2025/1/8 下午3:04
 * Version 1.0
 **/
@Slf4j
public class IndexTest {
    private RestHighLevelClient restHighLevelClient;

    @BeforeEach
    public void setUp() {
        restHighLevelClient = new RestHighLevelClient(
                RestClient.builder(HttpHost.create("localhost:9200"))
        );
    }

    @Test
    public void testConnect() throws IOException {
      log.info(this.restHighLevelClient.toString());
    }

    /***
     * 创建索引
     **/
    @Test
    public void createIndex() throws IOException {
        //创建request请求对象
        CreateIndexRequest createIndexRequest = new CreateIndexRequest("items");
        //准备请求参数
        createIndexRequest.source(FileUtil.readResourceFileToStr("/es/item_index.json"), XContentType.JSON);
        //发送请求
        restHighLevelClient.indices().create(createIndexRequest, RequestOptions.DEFAULT);
    }

    /***
     * 删除索引
     **/
    @Test
    public void deleteIndex() throws IOException {
        DeleteIndexRequest deleteIndexRequest = new DeleteIndexRequest("items");
        restHighLevelClient.indices().delete(deleteIndexRequest, RequestOptions.DEFAULT);
    }

    @Test
    public void testExistIndex() throws IOException {
        GetIndexRequest getIndexRequest = new GetIndexRequest("items");
        boolean exists = restHighLevelClient.indices().exists(getIndexRequest, RequestOptions.DEFAULT);
        log.info(exists?"索引库存在":"索引库不存在");
    }


    @AfterEach
    public void tearDown() throws IOException {
        this.restHighLevelClient.close();
    }
}