package server.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class MainController
{
  @GetMapping("/")
  public String greeting()
  {
    return "starting";
  }

  @GetMapping("/main")
  public String main()
  {
    return "main";
  }

//  @PostMapping("filter")
//  public RedirectView filter(@RequestParam List<String> articles)
//  {
//    resetParam();
//    if (articles.isEmpty())
//    {
//      return new RedirectView("/main");
//    }
//    filteredOperations = operationRepo.findByBalance(this.user.getBalance()).stream()
//                                      .filter(o -> o.getArticleName().equals(articles.get(0)))
//                                      .sorted(Comparator.comparing(Operation::getCreate_date).reversed())
//                                      .collect(Collectors.toList());
//    filter = true;
//    return new RedirectView("/main");
//  }
}
