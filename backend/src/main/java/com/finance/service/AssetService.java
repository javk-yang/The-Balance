package com.finance.service;

import com.finance.common.SecurityUtils;
import com.finance.dto.AssetRequest;
import com.finance.entity.Asset;
import com.finance.repository.AssetRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class AssetService {
    @Autowired
    private AssetRepository assetRepository;

    public List<Asset> getAllAssets() {
        return assetRepository.findByUserIdOrderByCreatedAtDesc(SecurityUtils.getCurrentUserId());
    }

    public Asset getAsset(Long id) {
        return findOwnedAsset(id);
    }

    public Asset createAsset(AssetRequest request) {
        Asset asset = new Asset();
        asset.setUserId(SecurityUtils.getCurrentUserId());
        copyRequest(asset, request, true);
        return assetRepository.save(asset);
    }

    public Asset updateAsset(Long id, AssetRequest request) {
        Asset asset = findOwnedAsset(id);
        copyRequest(asset, request, false);
        return assetRepository.save(asset);
    }

    @Transactional
    public void deleteAsset(Long id) {
        assetRepository.delete(findOwnedAsset(id));
    }

    public Map<String, Object> getOverview() {
        List<Asset> assets = getAllAssets();
        BigDecimal purchaseTotal = BigDecimal.ZERO;
        BigDecimal currentTotal = BigDecimal.ZERO;
        BigDecimal liquidatableValue = BigDecimal.ZERO;
        int activeCount = 0;
        for (Asset asset : assets) {
            if (!isHolding(asset)) continue;
            BigDecimal purchase = value(asset.getPurchasePrice());
            BigDecimal current = value(asset.getCurrentValue());
            purchaseTotal = purchaseTotal.add(purchase);
            currentTotal = currentTotal.add(current);
            if (Boolean.TRUE.equals(asset.getLiquidatable())) liquidatableValue = liquidatableValue.add(current);
            activeCount++;
        }
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("totalPurchasePrice", scale(purchaseTotal));
        result.put("totalCurrentValue", scale(currentTotal));
        BigDecimal depreciation = purchaseTotal.subtract(currentTotal).max(BigDecimal.ZERO);
        BigDecimal appreciation = currentTotal.subtract(purchaseTotal).max(BigDecimal.ZERO);
        result.put("totalDepreciation", scale(depreciation));
        result.put("totalAppreciation", scale(appreciation));
        result.put("liquidatableValue", scale(liquidatableValue));
        result.put("assetCount", activeCount);
        result.put("activeCount", activeCount);
        return result;
    }

    private Asset findOwnedAsset(Long id) {
        Long userId = SecurityUtils.getCurrentUserId();
        return assetRepository.findById(id).filter(asset -> userId.equals(asset.getUserId()))
                .orElseThrow(() -> new RuntimeException("资产不存在"));
    }

    private void copyRequest(Asset asset, AssetRequest request, boolean create) {
        BigDecimal purchasePrice = request.getPurchasePrice();
        BigDecimal currentValue = request.getCurrentValue() == null ? purchasePrice : request.getCurrentValue();
        if (purchasePrice == null || purchasePrice.compareTo(BigDecimal.ZERO) < 0) throw new RuntimeException("购入价格不能为负");
        if (currentValue.compareTo(BigDecimal.ZERO) < 0) throw new RuntimeException("当前价值不能为负");
        asset.setName(request.getName().trim());
        asset.setCategory(request.getCategory().trim());
        asset.setPurchasePrice(purchasePrice);
        asset.setCurrentValue(currentValue);
        asset.setLiquidatable(request.getLiquidatable() == null ? Boolean.TRUE : request.getLiquidatable());
        asset.setStatus(normalizeStatus(request.getStatus(), create ? "ACTIVE" : asset.getStatus()));
        asset.setPurchaseDate(request.getPurchaseDate());
        asset.setRemark(request.getRemark());
    }

    private String normalizeStatus(String status, String fallback) {
        if (status == null || status.isBlank()) return fallback;
        String normalized = status.toUpperCase();
        if (!normalized.equals("ACTIVE") && !normalized.equals("SOLD") && !normalized.equals("DISPOSED")) {
            throw new RuntimeException("资产状态必须为 ACTIVE、SOLD 或 DISPOSED");
        }
        return normalized;
    }

    private boolean isHolding(Asset asset) {
        return "ACTIVE".equalsIgnoreCase(asset.getStatus()) || "HOLDING".equalsIgnoreCase(asset.getStatus());
    }

    private BigDecimal value(BigDecimal number) { return number == null ? BigDecimal.ZERO : number; }
    private BigDecimal scale(BigDecimal number) { return number.setScale(2, RoundingMode.HALF_UP); }
}
