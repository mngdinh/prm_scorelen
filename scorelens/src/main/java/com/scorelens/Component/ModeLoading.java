package com.scorelens.Component;

import com.scorelens.DTOs.Request.ModeRequest;
import com.scorelens.DTOs.Response.ModeResponse;
import com.scorelens.Entity.Mode;
import com.scorelens.Service.ModeService;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

@Component
@Slf4j
@Getter
@Setter
public class ModeLoading {

    @Autowired
    private ModeService modeService;

    // Dùng CopyOnWriteArraySet để an toàn khi đọc/ghi đa luồng
    private final Set<Integer> modes = new CopyOnWriteArraySet<>();


//      Tự động load danh sách mode từ DB khi khởi động app
    @PostConstruct
    public void init() {
        log.info("🔄 Loading modes from database...");
        List<ModeResponse> allModes = modeService.getAll();

        for (ModeResponse mr : allModes) {
            modes.add(mr.getModeID());
        }

        log.info("✅ Loaded {} modes: {}", modes.size(), modes);
    }


//     * Thêm mới một mode — đồng thời lưu vào DB và cập nhật vào Set
    public void addMode(Mode request) {
        // Thêm vào Set nếu chưa có
        if (modes.add(request.getModeID())) {
            log.info("🆕 Added new mode ID {} into cache", request.getModeID());
        } else {
            log.info("⚠️ Mode ID {} already exists in cache", request.getModeID());
        }
    }

    public boolean isValidMode(int modeId) {
        return getModes().contains(modeId);
    }
}
