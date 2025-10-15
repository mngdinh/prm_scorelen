package com.scorelens.Service;

import com.scorelens.DTOs.Request.ModeRequest;
import com.scorelens.DTOs.Response.ModeResponse;
import com.scorelens.Entity.Mode;
import com.scorelens.Exception.AppException;
import com.scorelens.Exception.ErrorCode;
import com.scorelens.Mapper.ModeMapper;
import com.scorelens.Repository.ModeRepository;
import com.scorelens.Service.Filter.BaseSpecificationService;
import com.scorelens.Service.Interface.IModeService;
import jakarta.persistence.criteria.Predicate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

@Service
public class ModeService extends BaseSpecificationService<Mode, ModeResponse> implements IModeService {

    @Autowired
    private ModeRepository modeRepository;

    @Autowired
    private ModeMapper modeMapper;


    @Override
    protected JpaSpecificationExecutor<Mode> getRepository() {
        return modeRepository;
    }

    @Override
    protected Function<Mode, ModeResponse> getMapper() {
        return modeMapper::toResponse;
    }

    @Override
    protected Specification<Mode> buildSpecification(Map<String, Object> filters) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            Integer modeId = (Integer) filters.get("modeId");
            String queryType = (String) filters.get("queryType");

            if ("byId".equals(queryType)) predicates.add(cb.equal(root.get("modeID"), modeId));

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    @Override
    public ModeResponse createMode(ModeRequest request) {
        Mode mode = new Mode();
        mode.setName(request.getName());
        mode.setDescription(request.getDescription());
        mode.setActive(request.isActive());
        mode = modeRepository.save(mode);
        return modeMapper.toResponse(mode);
    }

    @Override
    public List<ModeResponse> getAll() {
        List<Mode> modes = modeRepository.findAll();
        return modeMapper.toResponses(modes);
    }

    @Override
    public ModeResponse getById(Integer id) {
        Mode mode = modeRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.MODE_NOT_FOUND));
        return modeMapper.toResponse(modeRepository.save(mode));
    }
    @Override
    public ModeResponse updateMode(Integer id, ModeRequest request) {
        Mode mode = modeRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.MODE_NOT_FOUND));

        mode.setName(request.getName());
        mode.setDescription(request.getDescription());
        mode.setActive(request.isActive());
        return modeMapper.toResponse(modeRepository.save(mode));
    }

    @Override
    public Integer delete(Integer id) {
        if (!modeRepository.existsById(id)) {
            throw new AppException(ErrorCode.MODE_NOT_FOUND);
        }
        modeRepository.deleteById(id);
        return id;
    }
}

