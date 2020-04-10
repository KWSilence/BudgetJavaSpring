package server.controller;


import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import server.entities.Article;
import server.repos.ArticleRepo;

import java.util.Map;

@Controller
public class MainController {
    private final ArticleRepo articleRepo;

    public MainController(ArticleRepo articleRepo) {
        this.articleRepo = articleRepo;
    }

    @GetMapping("/")
    public String greeting() {
        return "starting";
    }

    @GetMapping("/main")
    public String main(Map<String, Object> model) {
        Iterable<Article> articles = articleRepo.findAll();
        model.put("articles", articles);
        return "main";
    }

    @PostMapping("add")
    public String add(/*@AuthenticationPrincipal User user, */@RequestParam String name, Map<String, Object> model) {
        Article article = new Article(name/*, user*/);
        if (articleRepo.findByName(name).isEmpty()) {
            articleRepo.save(article);
        } else {
            model.put("error", "this name already exist");
        }
        Iterable<Article> articles = articleRepo.findAll();
        model.put("articles", articles);
        return "main";
    }

    @PostMapping("filter")
    public String filter(@RequestParam String filter, Map<String, Object> model) {
        Iterable<Article> articles;
        if (filter != null && !filter.isEmpty()) {
            articles = articleRepo.findByName(filter);
        } else {
            articles = articleRepo.findAll();
        }

        model.put("articles", articles);
        return "main";
    }

    @PostMapping("truncate")
    public String truncate(Map<String, Object> model) {
        articleRepo.truncateTable();
        model.put("articles", articleRepo.findAll());
        return "main";
    }

}