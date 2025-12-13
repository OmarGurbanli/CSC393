package com.rentacar;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class ExtraService {
    private final ExtraRepository extraRepository;

    public List<Extra> getAllExtras() {
        return extraRepository.findAll();
    }

    public Extra getExtraById(Long id) {
        return extraRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Extra not found with id: " + id));
    }

    public Extra getExtraByName(String name) {
        return extraRepository.findByName(name)
                .orElseThrow(() -> new RuntimeException("Extra not found with name: " + name));
    }

    public Extra createExtra(Extra extra) {
        if (extraRepository.findByName(extra.getName()).isPresent()) {
            throw new RuntimeException("Extra with name " + extra.getName() + " already exists");
        }
        return extraRepository.save(extra);
    }

    public Extra updateExtra(Long id, Extra extraDetails) {
        Extra extra = getExtraById(id);
        extra.setName(extraDetails.getName());
        extra.setPrice(extraDetails.getPrice());

        return extraRepository.save(extra);
    }

    public boolean deleteExtra(Long id) {
        Extra extra = getExtraById(id);

        if (!extra.getReservations().isEmpty()) {
            return false;
        }

        extraRepository.delete(extra);
        return true;
    }
}