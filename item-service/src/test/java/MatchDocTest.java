import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.json.JSONUtil;
import com.hmall.item.ItemApplication;
import com.hmall.item.domain.po.ItemDoc;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpHost;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.elasticsearch.search.sort.SortOrder;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * ClassName IndexDocTest
 *
 * @author qml
 * Date 2025/1/8 下午4:27
 * Version 1.0
 **/

@SpringBootTest(classes = ItemApplication.class)
@Slf4j
public class MatchDocTest {

    private static  RestHighLevelClient restHighLevelClient;

    private static final String indexName = "items";

    @BeforeEach
    public void setUp() {
        restHighLevelClient = new RestHighLevelClient(RestClient.builder(HttpHost.create("localhost:9200")));
    }

    @Test
    void testMatchAll() throws IOException {
        //1.创建request
        SearchRequest request = new SearchRequest(indexName);
        //2.组织请求参数
//        request.source().query(QueryBuilders.matchAllQuery());
//        request.source().query(QueryBuilders.matchQuery("name","脱脂牛奶"));
//        request.source().query(QueryBuilders.multiMatchQuery("脱脂牛奶","name","brand"));
//        request.source().query(QueryBuilders.rangeQuery("price").gte(10000));


        //准备bool查询
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
        boolQueryBuilder.must(QueryBuilders.matchQuery("name","脱脂牛奶"));
        boolQueryBuilder.filter(QueryBuilders.termQuery("brand","德亚"));
        boolQueryBuilder.filter(QueryBuilders.rangeQuery("price").gte(100));
        request.source()
                //bool查询
                .query(boolQueryBuilder)
                //分页查询
                .from(1).size(100)
                //排序
                .sort("price", SortOrder.DESC)
                //高亮查询
                .highlighter(new HighlightBuilder().field("name").preTags("<em>").postTags("</em>"));
        //3.发送请求
        SearchResponse response = restHighLevelClient.search(request, RequestOptions.DEFAULT);
        //4.解析响应
        handResponse(response);
    }

    private void handResponse(SearchResponse response) {
        SearchHits searchHits  = response.getHits();
        //1.获取总条数
        long value = searchHits.getTotalHits().value;
        log.info("总共搜索到{}数据",value);
        //2.遍历结果数组
        SearchHit[] hits = searchHits.getHits();
        for (SearchHit hit : hits) {
            String sourceAsString = hit.getSourceAsString();
            ItemDoc item = JSONUtil.toBean(sourceAsString, ItemDoc.class);

            //获取高亮结果
            Map<String, HighlightField> highlightFields = hit.getHighlightFields();
            if(CollectionUtil.isNotEmpty(highlightFields)){
                HighlightField highlightField = highlightFields.get("name");
                if(highlightField != null){
                    String name = highlightField.getFragments()[0].toString();
                    item.setName(name);
                }
            }
            log.info(item.toString());
        }
    }


    @Test
    void testAgg() throws IOException {
        SearchRequest request = new SearchRequest(indexName);
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery()
                .filter(QueryBuilders.termQuery("category", "手机"))
                .filter(QueryBuilders.rangeQuery("price").gte(1000));
        request.source().query(boolQueryBuilder).size(0);
        request.source().aggregation(AggregationBuilders.terms("brandAgg").field("brand").size(5));

        SearchResponse response = restHighLevelClient.search(request, RequestOptions.DEFAULT);
        Aggregations aggregations = response.getAggregations();
        Terms brandAgg = aggregations.get("brandAgg");
        List<? extends Terms.Bucket> buckets = brandAgg.getBuckets();
        for (Terms.Bucket bucket : buckets) {
            // 5.4.获取桶内key
            String brand = bucket.getKeyAsString();
            System.out.print("brand = " + brand);
            long count = bucket.getDocCount();
            System.out.println("; count = " + count);
        }
    }

    @SneakyThrows
    @AfterEach
    public void tearDown() {
        restHighLevelClient.close();
    }
}