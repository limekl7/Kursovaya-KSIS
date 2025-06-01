package ru.codekitchen.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import ru.codekitchen.entity.dto.RecordsContainerDto;
import ru.codekitchen.service.RecordService;

@Controller
public class CommonController {
    private final RecordService recordService;

    @Autowired
    public CommonController(RecordService recordService) {
        this.recordService = recordService;
    }

    @RequestMapping("/")
    public String redirectToHomePage() {
        return "redirect:/home";
    }

    @RequestMapping("/home")
    public String getMainPage(Model model,
                              @RequestParam(name = "filter", required = false) String filterMode,
                              @RequestParam(name = "minPrice", required = false) String minPrice,
                              @RequestParam(name = "maxPrice", required = false) String maxPrice,
                              @RequestParam(name = "sort", required = false) String sort) {
        RecordsContainerDto container = recordService.findAllRecords(filterMode, false);
        model.addAttribute("records", container.getRecords());
        model.addAttribute("filterMode", filterMode);
        model.addAttribute("minPrice", minPrice);
        model.addAttribute("maxPrice", maxPrice);
        model.addAttribute("sort", sort);
        return "main-page";
    }

    @RequestMapping("/exchanges")
    public String getExchangesPage(Model model,
                                   @RequestParam(name = "filter", required = false) String filterMode) {
        String[] exchanges = {"binance", "bybit", "kraken", "bitfinex", "huobi"}; // Добавляем Huobi
        model.addAttribute("exchanges", exchanges);
        model.addAttribute("filterMode", filterMode);
        return "exchanges-page";
    }

    @RequestMapping("/exchange/{exchange}")
    public String getExchangePage(@PathVariable String exchange, Model model,
                                  @RequestParam(name = "filter", required = false) String filterMode,
                                  @RequestParam(name = "minPrice", required = false) String minPrice,
                                  @RequestParam(name = "maxPrice", required = false) String maxPrice,
                                  @RequestParam(name = "sort", required = false) String sort) {
        RecordsContainerDto container = recordService.findExchangeRecords(exchange, filterMode, false);
        model.addAttribute("records", container.getRecords());
        model.addAttribute("exchange", exchange.substring(0, 1).toUpperCase() + exchange.substring(1));
        model.addAttribute("filterMode", filterMode);
        model.addAttribute("minPrice", minPrice);
        model.addAttribute("maxPrice", maxPrice);
        model.addAttribute("sort", sort);
        return "exchange-" + exchange; // Используем разные JSP для разных бирж: exchange-binance, exchange-bybit, exchange-kraken, exchange-bitfinex, exchange-huobi
    }

    @RequestMapping(value = "/refresh-data", method = RequestMethod.POST)
    @ResponseBody
    public RecordsContainerDto refreshData() {
        return recordService.findAllRecords(null, true);
    }
}