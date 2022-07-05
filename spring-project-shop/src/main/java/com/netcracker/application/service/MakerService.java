package com.netcracker.application.service;

import com.netcracker.application.service.model.entity.Maker;
import com.netcracker.application.service.repository.MakerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.management.InstanceAlreadyExistsException;
import java.math.BigInteger;
import java.sql.SQLException;
import java.util.*;

@Component
public class MakerService {
    private final Map<BigInteger, Maker> makers = new HashMap<>();
    private final MakerRepository makerRepository;

    @Autowired
    public MakerService(MakerRepository makerRepository) {
        this.makerRepository = makerRepository;
    }

    private void fill() {
        if (makers.isEmpty()) {
            for (Maker maker : makerRepository.findAll()) {
                makers.put(maker.getId(), maker);
            }
        }
    }

    public List<Maker> getAll() {
        fill();
        return new ArrayList<>(makers.values());
    }

    public Maker getById(BigInteger id) {
        fill();
        return makers.get(id);
    }

    public void add(Maker maker) throws SQLException {
        if (Objects.isNull(maker.getId())) {
            if (Objects.nonNull(makerRepository.findByName(maker.getName()))) {
                throw new SQLException("Maker with that name already exists");
            }
        }
        if (maker.getName().equals("")) {
            throw new SQLException("Maker name must be provided");
        }
        if (maker.getProductsAmount() < 0) {
            throw new SQLException("Products amount can not be < 0");
        }
        makerRepository.save(maker);
        makers.clear();
    }

    public void delete(BigInteger id) {
        makerRepository.delete(makers.get(id));
        makers.remove(id);
    }

    public void update(Maker maker) {
        Maker makerForUpdate = makers.get(maker.getId());
        makerForUpdate.setProductsAmount(maker.getProductsAmount());
        makerRepository.save(makerForUpdate);
        makers.clear();
    }
}
