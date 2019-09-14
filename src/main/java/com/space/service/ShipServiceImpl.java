package com.space.service;

import com.space.controller.ShipOrder;
import com.space.model.Ship;
import com.space.model.ShipType;
import com.space.repository.ShipRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;

@Service
public class ShipServiceImpl implements ShipService {
    private ShipRepository repository;

    @Autowired
    public ShipServiceImpl(ShipRepository repository) {
        this.repository = repository;
    }

    @Transactional
    @Override
    public List<Ship> getShipsList(String name,
                                   String planet,
                                   ShipType shipType,
                                   Long after,
                                   Long before,
                                   Boolean isUsed,
                                   Double minSpeed,
                                   Double maxSpeed,
                                   Integer minCrewSize,
                                   Integer maxCrewSize,
                                   Double minRating,
                                   Double maxRating,
                                   ShipOrder order,
                                   Integer pageNumber,
                                   Integer pageSize) {
        Specification<Ship> specification = filterByName(name).and(filterByPlanet(planet)).and(filterByShipType(shipType)).
                and(filterByProdDate(after, before)).and(filterByUsing(isUsed)).and(filterBySpeed(minSpeed, maxSpeed)).
                and(filterByCrewSize(minCrewSize, maxCrewSize)).and(filterByRating(minRating, maxRating));
        Pageable pageable = PageRequest.of(pageNumber, pageSize, Sort.by(order.getFieldName()));
        return repository.findAll(specification, pageable).getContent();
    }

    @Transactional
    @Override
    public Integer getShipsCount(String name,
                                 String planet,
                                 ShipType shipType,
                                 Long after,
                                 Long before,
                                 Boolean isUsed,
                                 Double minSpeed,
                                 Double maxSpeed,
                                 Integer minCrewSize,
                                 Integer maxCrewSize,
                                 Double minRating,
                                 Double maxRating) {
        Specification<Ship> specification = filterByName(name).and(filterByPlanet(planet)).and(filterByShipType(shipType)).
                and(filterByProdDate(after, before)).and(filterByUsing(isUsed)).and(filterBySpeed(minSpeed, maxSpeed)).
                and(filterByCrewSize(minCrewSize, maxCrewSize)).and(filterByRating(minRating, maxRating));
        return repository.findAll(specification).size();
    }

    @Transactional
    @Override
    public Ship createShip(Ship requestBody) {
        if (requestBody == null) {
            return null;
        }
        Ship ship = new Ship();
        if (checkValidParam(requestBody)) {
            ship.setName(requestBody.getName());
            ship.setPlanet(requestBody.getPlanet());
            if (requestBody.isUsed() == null) {
                ship.setUsed(false);
            } else {
                ship.setUsed(requestBody.isUsed());
            }
            ship.setProdDate(requestBody.getProdDate());
            ship.setShipType(requestBody.getShipType());
            ship.setSpeed(requestBody.getSpeed());
            ship.setCrewSize(requestBody.getCrewSize());
        }
        Double rating = calculateRating(ship);
        ship.setRating(rating);
        repository.save(ship);
        return ship;
    }

    @Transactional
    @Override
    public Ship getShip(Long id) {
        if (id <= 0) throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        if (!repository.findById(id).isPresent()) {
            throw  new ResponseStatusException(HttpStatus.NOT_FOUND);
        }

        return repository.findById(id).get();
    }

    @Transactional
    @Override

    public Ship updateShip(Long id, Ship requestBody) {
        Ship ship = getShip(id);

        if (requestBody.getName() == null &&
                requestBody.getPlanet() == null &&
                requestBody.getShipType() == null &&
                requestBody.getProdDate() == null &&
                requestBody.getSpeed() == null &&
                requestBody.getCrewSize() == null) return ship;

        if (requestBody.getName() != null)
            if (!requestBody.getName().isEmpty()
                    && requestBody.getName().length() <= 50)
                ship.setName(requestBody.getName());
            else throw new ResponseStatusException(HttpStatus.BAD_REQUEST);

        if (requestBody.getPlanet() != null)
            if (requestBody.getPlanet().length() <= 50)
                ship.setPlanet(requestBody.getPlanet());
            else throw new ResponseStatusException(HttpStatus.BAD_REQUEST);

        if (requestBody.getShipType() != null)
            ship.setShipType(requestBody.getShipType());

        if (requestBody.getProdDate() != null) {
            if (requestBody.getProdDate().getTime() > 0
                    && checkProdDate(requestBody.getProdDate()))
                ship.setProdDate(requestBody.getProdDate());
            else throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }

        if (requestBody.isUsed() != null)
            ship.setUsed(requestBody.isUsed());

        if (requestBody.getSpeed() != null)
            if (requestBody.getSpeed() >= 0.01 || requestBody.getSpeed() <= 0.99)
                ship.setSpeed(requestBody.getSpeed());
            else throw new ResponseStatusException(HttpStatus.BAD_REQUEST);

        if (requestBody.getCrewSize() != null)
            if (requestBody.getCrewSize() >= 1 && requestBody.getCrewSize() <= 9999)
                ship.setCrewSize(requestBody.getCrewSize());
            else throw new ResponseStatusException(HttpStatus.BAD_REQUEST);

        Double rating = calculateRating(ship);
        ship.setRating(rating);
        repository.saveAndFlush(ship);

        return ship;
    }

    @Transactional
    @Override
    public void deleteShip(Long id) {
        if (id <= 0) throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        if (!repository.existsById(id)) throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        repository.deleteById(id);
    }

    private Specification<Ship> filterByName(String name) {
        return (root, query, criteriaBuilder) -> name == null ? null :
                criteriaBuilder.like(root.get("name"), "%" + name + "%");
    }

    private Specification<Ship> filterByPlanet(String planet) {
        return (root, query, criteriaBuilder) -> planet == null ? null :
                criteriaBuilder.like(root.get("planet"), "%" + planet + "%");
    }

    private Specification<Ship> filterByShipType(ShipType shipType) {
        return (root, query, criteriaBuilder) -> shipType == null ? null :
                criteriaBuilder.equal(root.get("shipType"), shipType);
    }

    private Specification<Ship> filterByProdDate(Long after, Long before) {
        return (root, query, criteriaBuilder) -> {
            if(after == null && before == null) {
                return null;
            }
            if (after == null) {
                Date beforeDate = new Date(before);
                return criteriaBuilder.lessThanOrEqualTo(root.get("prodDate"), beforeDate);
            }
            if (before == null) {
                Date afterDate = new Date(after);
                return criteriaBuilder.greaterThanOrEqualTo(root.get("prodDate"), afterDate);
            }
            Date afterDate = new Date(after);
            Date beforeDate = new Date(before);
            return criteriaBuilder.between(root.get("prodDate"), afterDate, beforeDate);
        };
    }

    private Specification<Ship> filterByUsing(Boolean isUsed) {
        return (root, query, criteriaBuilder) -> isUsed == null ? null :
                isUsed ? criteriaBuilder.isTrue(root.get("isUsed")) : criteriaBuilder.isFalse(root.get("isUsed"));
    }

    private Specification<Ship> filterBySpeed(Double minSpeed, Double maxSpeed) {
        return (root, query, criteriaBuilder) -> {
            if (minSpeed == null && maxSpeed == null) {
                return null;
            }
            if (minSpeed == null) {
                return criteriaBuilder.lessThanOrEqualTo(root.get("speed"), maxSpeed);
            }
            if (maxSpeed == null) {
                return criteriaBuilder.greaterThanOrEqualTo(root.get("speed"), minSpeed);
            }
            return criteriaBuilder.between(root.get("speed"), minSpeed, maxSpeed);
        };
    }

    private Specification<Ship> filterByCrewSize(Integer minCrewSize, Integer maxCrewSize) {
        return (root, query, criteriaBuilder) -> {
            if (minCrewSize == null && maxCrewSize == null) {
                return null;
            }
            if (minCrewSize == null) {
                return criteriaBuilder.lessThanOrEqualTo(root.get("crewSize"), maxCrewSize);
            }
            if (maxCrewSize == null) {
                return criteriaBuilder.greaterThanOrEqualTo(root.get("crewSize"), minCrewSize);
            }
            return criteriaBuilder.between(root.get("crewSize"), minCrewSize, maxCrewSize);
        };
    }

    private Specification<Ship> filterByRating(Double minRating, Double maxRating) {
        return (root, query, criteriaBuilder) -> {
            if (minRating == null && maxRating == null) {
                return null;
            }
            if (minRating == null) {
                return criteriaBuilder.lessThanOrEqualTo(root.get("rating"), maxRating);
            }
            if (maxRating == null) {
                return criteriaBuilder.greaterThanOrEqualTo(root.get("rating"), minRating);
            }
            return criteriaBuilder.between(root.get("rating"), minRating, maxRating);
        };
    }

    private boolean checkValidParam(Ship ship) {
        if (ship.getName() == null || ship.getName().equals("")
                || ship.getName().length() > 50 || ship.getPlanet() == null
                || ship.getPlanet().equals("")|| ship.getPlanet().length() > 50
                || ship.getProdDate() == null
                || ship.getProdDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate().getYear() < 2800
                || ship.getProdDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate().getYear() > 3019
                || ship.getProdDate().getTime() < 0 || ship. getSpeed() == null
                || ship.getSpeed() < 0.01 || ship.getSpeed() > 0.99
                || ship.getCrewSize() == null
                || ship.getCrewSize() < 1 || ship.getCrewSize() > 9999) {

            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }
        return true;
    }

    private boolean checkProdDate(Date date) {
        Calendar calendar = new GregorianCalendar();
        calendar.setTime(date);
        return  calendar.get(Calendar.YEAR) >= 2800 && calendar.get(Calendar.YEAR) <= 3019;
    }

    private Double calculateRating(Ship ship) {
        double k = ship.isUsed() ? 0.5 : 1;
        LocalDate localDate = ship.getProdDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        if (localDate.getYear() > 3019) throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        double rating = (80 * ship.getSpeed() * k / (3020 - localDate.getYear()));
        return Math.round(rating*100.0)/100.0;
    }
}
