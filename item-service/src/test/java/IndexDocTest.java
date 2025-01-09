import cn.hutool.core.bean.BeanUtil;
import cn.hutool.json.JSONUtil;
import com.hmall.item.domain.po.Item;
import com.hmall.item.domain.po.ItemDoc;
import com.hmall.item.service.IItemService;
import lombok.SneakyThrows;
import org.apache.http.HttpHost;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentType;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;

/**
 * ClassName IndexDocTest
 *
 * @author qml
 * Date 2025/1/8 下午4:27
 * Version 1.0
 **/

@SpringBootTest
public class IndexDocTest {

    private static  RestHighLevelClient restHighLevelClient;

    @Autowired
    private IItemService itemService;

    @BeforeEach
    public void setUp() {
        restHighLevelClient = new RestHighLevelClient(RestClient.builder(HttpHost.create("localhost:9200")));
    }

    @Test
    void testAddDoc() throws IOException {
        // 1.根据id查询商品数据
        Item item = itemService.getById(100002644680L);
        // 2.转换为文档类型
        ItemDoc itemDoc = BeanUtil.copyProperties(item, ItemDoc.class);
        // 3.将ItemDTO转json
        String doc = JSONUtil.toJsonStr(itemDoc);

        // 1.准备Request对象
        IndexRequest indexRequest = new IndexRequest("items").id(itemDoc.getId());
        // 2.准备Json文档
        indexRequest.source(doc, XContentType.JSON);
        // 3.发送请求
        restHighLevelClient.index(indexRequest, RequestOptions.DEFAULT);
    }

    @SneakyThrows
    @AfterEach
    public void tearDown() {
        restHighLevelClient.close();
    }
}