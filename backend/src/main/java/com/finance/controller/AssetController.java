package com.finance.controller;

import com.finance.common.Result;
import com.finance.dto.AssetRequest;
import com.finance.entity.Asset;
import com.finance.service.AssetService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/assets")
public class AssetController {
    @Autowired
    private AssetService assetService;

    @GetMapping
    public Result<List<Asset>> list() {
        return Result.success(assetService.getAllAssets());
    }

    @GetMapping("/overview")
    public Result<Map<String, Object>> overview() {
        return Result.success(assetService.getOverview());
    }

    @GetMapping("/{id}")
    public Result<Asset> detail(@PathVariable Long id) {
        return Result.success(assetService.getAsset(id));
    }

    @PostMapping
    public Result<Asset> create(@Valid @RequestBody AssetRequest request) {
        return Result.success(assetService.createAsset(request));
    }

    @PutMapping("/{id}")
    public Result<Asset> update(@PathVariable Long id, @Valid @RequestBody AssetRequest request) {
        return Result.success(assetService.updateAsset(id, request));
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        assetService.deleteAsset(id);
        return Result.success();
    }
}
