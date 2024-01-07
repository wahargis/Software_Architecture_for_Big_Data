package io.collective.endpoints;

import io.collective.articles.ArticleDataGateway;
import io.collective.articles.ArticleInfo;
import io.collective.articles.ArticleRecord;
import io.collective.restsupport.RestTemplate;
import io.collective.rss.RSS;
import io.collective.rss.Item;
import io.collective.workflow.Worker;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;

import java.io.IOException;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

public class EndpointWorker implements Worker<EndpointTask> {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final RestTemplate template;
    private final ArticleDataGateway gateway;
    private final Random sequence = new Random();

    public EndpointWorker(RestTemplate template, ArticleDataGateway gateway) {
        this.template = template;
        this.gateway = gateway;
    }

    @NotNull
    @Override
    public String getName() {
        return "ready";
    }

    @Override
    public void execute(EndpointTask task) throws IOException {
        String response = template.get(task.getEndpoint(), task.getAccept());
        gateway.clear();

        { // todo - map rss results to an article infos collection and save articles infos to the article gateway
            
            // map rss results to ArticleInfos collection
            RSS rss = new XmlMapper().readValue(response, RSS.class);
            List<Item> articles = rss.getChannel().getItem();
            List<ArticleInfo> articleInfos = articles.stream()
                .map(article -> new ArticleInfo(sequence.nextInt(), article.getTitle()))
                .collect(Collectors.toList());

            // save ArticleInfos to ArticleGateway
            for (ArticleInfo articleInfo : articleInfos) {
                gateway.save(articleInfo.getTitle());

            }
        }
    }
};
