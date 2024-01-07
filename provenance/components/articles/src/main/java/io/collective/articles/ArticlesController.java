package io.collective.articles;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.collective.restsupport.BasicHandler;
import org.eclipse.jetty.server.Request;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ArticlesController extends BasicHandler {
    private Logger logger = LoggerFactory.getLogger(this.getClass());
    private final ArticleDataGateway gateway;
    private final ObjectMapper mapper;

    public ArticlesController(ObjectMapper mapper, ArticleDataGateway gateway) {
        super(mapper);
        this.gateway = gateway;
        this.mapper = mapper;
    }

    @Override
    public void handle(String target, Request request, HttpServletRequest servletRequest, HttpServletResponse servletResponse) {
        get("/articles", List.of("application/json", "text/html"), request, servletResponse, () -> {

            { // todo - query the articles gateway for *all* articles, map record to infos, and send back a collection of article infos
                
                // query the gateway for all articles which are returned as a list of class ArticleRecord
                List<ArticleRecord> articles = gateway.findAll();

                // stream the articles and map the contents to list of ArticleInfo class
                List<ArticleInfo> articleInfos = articles.stream()
                    .map(article -> new ArticleInfo(article.getId(), article.getTitle()))
                    .collect(Collectors.toList());
                
                // send collection of article infos; write the articleInfos to a servlet http response
                writeResponse(servletResponse, articleInfos);
            }

        });

        get("/available", List.of("application/json"), request, servletResponse, () -> {

            { // todo - query the articles gateway for *available* articles, map records to infos, and send back a collection of article infos

                // query the gateway for available articles which are returned as a list of class ArticleRecord
                List<ArticleRecord> articles = gateway.findAvailable();

                // stream the articles and map the contents to list of ArticleInfo class
                List<ArticleInfo> articleInfos = articles.stream()
                    .map(article -> new ArticleInfo(article.getId(), article.getTitle()))
                    .collect(Collectors.toList());

                // send collection of article infos; write the articleInfos to a servlet http response
                writeResponse(servletResponse, articleInfos);


            }

        });
    }

    private void writeResponse(HttpServletResponse response, List<ArticleInfo> articleInfos) {
        //Description:
        //   a helper method for writing an http response of our articleInfos
        //Parameters:
        //   response | HttpServletResponse | an http response from the javax servlet package
        //   articles | List<ArticleInfo> | a list of ArticleInfos containing article ID and title
        //Return:
        //   void
        try {
            response.setContentType("application/json");
            response.setStatus(HttpServletResponse.SC_OK);
            mapper.writeValue(response.getOutputStream(), articleInfos);
        } catch (IOException e) {
            // Log the exception for debugging purposes
            logger.error("Error writing response: " + e.getMessage(), e);

            // Set an appropriate HTTP error status
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    };
}
