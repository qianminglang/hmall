import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hmall.item.ItemApplication;
import com.hmall.item.domain.po.Item;
import com.hmall.item.domain.po.ItemDoc;
import com.hmall.item.service.IItemService;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpHost;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;
import java.util.List;

/**
 * ClassName IndexDocTest
 *
 * @author qml
 * Date 2025/1/8 下午4:27
 * Version 1.0
 **/

@SpringBootTest(classes = ItemApplication.class)
@Slf4j
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

    @Test
    void batchAddDoc() throws IOException {
        int pageNum=1;
        int pageSize=1000;
        while (true){
            Page<Item> page = itemService.lambdaQuery().eq(Item::getStatus, 1).page(new Page<>(pageNum, pageSize));
            List<Item> itemList = page.getRecords();
            if(CollectionUtil.isEmpty(itemList)){
                return;
            }
            log.info("加载到第{}页数据，共{}条",pageNum,itemList.size()) ;
            // 1.准备Request对象
            BulkRequest bulkRequest = new BulkRequest("items");
            for (Item item : itemList) {
                // 2.转换为文档类型
                ItemDoc itemDoc = BeanUtil.copyProperties(item, ItemDoc.class);
                bulkRequest.add(new IndexRequest().id(itemDoc.getId()).source(JSONUtil.toJsonStr(itemDoc), XContentType.JSON));
            }
            // 3.发送请求
            restHighLevelClient.bulk(bulkRequest,RequestOptions.DEFAULT);
            //翻页
            pageNum++;
        }
    }

    @Test
    void testQuery() throws IOException {
        SearchRequest searchRequest = new SearchRequest("items");
        searchRequest.source(new SearchSourceBuilder().query(QueryBuilders.matchAllQuery()));
        SearchResponse searchResponse = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);
        SearchHits hits = searchResponse.getHits();
        SearchHit[] searchHits = hits.getHits();

        for (SearchHit hit : searchHits) {
            // 从每个 hit 中获取文档的源数据
            String documentSource = hit.getSourceAsString();
            ItemDoc bean = JSONUtil.toBean(documentSource, ItemDoc.class);
            // 处理文档数据，这里只是简单示例
            System.out.println(bean);
        }
    }

    @SneakyThrows
    @AfterEach
    public void tearDown() {
        restHighLevelClient.close();
    }
}