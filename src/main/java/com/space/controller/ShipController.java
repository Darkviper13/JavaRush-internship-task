package com.space.controller;

import com.space.model.Ship;
import com.space.model.ShipType;
import com.space.service.ShipService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/rest")
public class ShipController {
    private ShipService shipService;

    @Autowired
    public ShipController(ShipService shipService) {
        this.shipService = shipService;
    }

    @GetMapping(path = "/ships")
    public List<Ship> getShipsList(@RequestParam(value = "name", required = false) String name,
                                   @RequestParam(value = "planet", required = false) String planet,
                                   @RequestParam(value = "shipType",required = false) ShipType shipType,
                                   @RequestParam(value = "after", required = false) Long after,
                                   @RequestParam(value = "before", required = false) Long before,
                                   @RequestParam(value = "isUsed", required = false) Boolean isUsed,
                                   @RequestParam(value = "minSpeed", required = false) Double minSpeed,
                                   @RequestParam(value = "maxSpeed", required = false) Double maxSpeed,
                                   @RequestParam(value = "minCrewSize", required = false) Integer minCrewSize,
                                   @RequestParam(value = "maxCrewSize", required = false) Integer maxCrewSize,
                                   @RequestParam(value = "minRating", required = false) Double minRating,
                                   @RequestParam(value = "maxRating", required = false) Double maxRating,
                                   @RequestParam(value = "order", required = false, defaultValue = "ID") ShipOrder order,
                                   @RequestParam(value = "pageNumber", required = false, defaultValue = "0") Integer pageNumber,
                                   @RequestParam(value = "pageSize", required = false, defaultValue = "3") Integer pageSize) {
        return shipService.getShipsList(name, planet, shipType, after, before, isUsed, minSpeed,
                maxSpeed, minCrewSize, maxCrewSize, minRating, maxRating, order, pageNumber, pageSize);
    }

    @GetMapping(path = "/ships/count")
    public Integer getShipsCount(@RequestParam(value = "name", required = false) String name,
                                 @RequestParam(value = "planet", required = false) String planet,
                                 @RequestParam(value = "shipType",required = false) ShipType shipType,
                                 @RequestParam(value = "after", required = false) Long after,
                                 @RequestParam(value = "before", required = false) Long before,
                                 @RequestParam(value = "isUsed", required = false) Boolean isUsed,
                                 @RequestParam(value = "minSpeed", required = false) Double minSpeed,
                                 @RequestParam(value = "maxSpeed", required = false) Double maxSpeed,
                                 @RequestParam(value = "minCrewSize", required = false) Integer minCrewSize,
                                 @RequestParam(value = "maxCrewSize", required = false) Integer maxCrewSize,
                                 @RequestParam(value = "minRating", required = false) Double minRating,
                                 @RequestParam(value = "maxRating", required = false) Double maxRating) {
        return shipService.getShipsCount(name, planet, shipType, after, before, isUsed,
                minSpeed, maxSpeed, minCrewSize, maxCrewSize, minRating, maxRating);
    }

    @PostMapping(path = "/ships")
    public Ship createShip(@RequestBody Ship requestBody) {
        return shipService.createShip(requestBody);
    }

    @GetMapping(path = "/ships/{id}")
    public Ship getShip(@PathVariable("id") Long id) {
        return shipService.getShip(id);
    }

    @PostMapping(path = "/ships/{id}")
    public Ship updateShip(@PathVariable("id") Long id, @RequestBody Ship requestBody) {
        return shipService.updateShip(id, requestBody);
    }

    @DeleteMapping(path = "/ships/{id}")
    public void deleteShip(@PathVariable("id") Long id) {
        shipService.deleteShip(id);
    }

}
