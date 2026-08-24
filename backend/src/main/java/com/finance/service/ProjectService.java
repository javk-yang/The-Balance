package com.finance.service;

import com.finance.common.SecurityUtils;
import com.finance.dto.ProjectRequest;
import com.finance.entity.Project;
import com.finance.repository.ProjectRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class ProjectService {
    @Autowired
    private ProjectRepository projectRepository;

    public List<Project> getAllProjects() {
        Long userId = SecurityUtils.getCurrentUserId();
        List<Project> projects = projectRepository.findByUserIdOrderByCreatedAtDesc(userId);
        projects.forEach(Project::calculateStats);
        return projects;
    }

    public Project getProject(Long id) {
        Project project = findOwnedProject(id, SecurityUtils.getCurrentUserId());
        project.calculateStats();
        return project;
    }

    public Project createProject(ProjectRequest request) {
        Project project = new Project();
        project.setUserId(SecurityUtils.getCurrentUserId());
        copyRequest(project, request, true);
        Project saved = projectRepository.save(project);
        saved.calculateStats();
        return saved;
    }

    public Project updateProject(Long id, ProjectRequest request) {
        Project project = findOwnedProject(id, SecurityUtils.getCurrentUserId());
        copyRequest(project, request, false);
        Project saved = projectRepository.save(project);
        saved.calculateStats();
        return saved;
    }

    @Transactional
    public void deleteProject(Long id) {
        projectRepository.delete(findOwnedProject(id, SecurityUtils.getCurrentUserId()));
    }

    public Map<String, Object> getOverview() {
        List<Project> projects = projectRepository.findByUserIdOrderByCreatedAtDesc(SecurityUtils.getCurrentUserId());
        BigDecimal totalContractValue = BigDecimal.ZERO;
        BigDecimal totalReceived = BigDecimal.ZERO;
        BigDecimal totalPending = BigDecimal.ZERO;
        BigDecimal currentMonthReceived = BigDecimal.ZERO;
        int activeProjectCount = 0;
        YearMonth month = YearMonth.now();
        for (Project project : projects) {
            project.calculateStats();
            totalContractValue = totalContractValue.add(value(project.getPrice()));
            totalReceived = totalReceived.add(value(project.getTotalReceived()));
            totalPending = totalPending.add(value(project.getPendingAmount()));
            if ("ACTIVE".equalsIgnoreCase(project.getStatus())) activeProjectCount++;
            LocalDate contractDate = project.getContractDate();
            if (contractDate != null && YearMonth.from(contractDate).equals(month)) {
                currentMonthReceived = currentMonthReceived.add(value(project.getTotalReceived()));
            }
        }
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("totalContractValue", scale(totalContractValue));
        result.put("totalReceived", scale(totalReceived));
        result.put("totalPending", scale(totalPending));
        result.put("currentMonthReceived", scale(currentMonthReceived));
        result.put("projectCount", projects.size());
        result.put("activeProjectCount", activeProjectCount);
        return result;
    }

    private Project findOwnedProject(Long id, Long userId) {
        return projectRepository.findById(id).filter(p -> userId.equals(p.getUserId()))
                .orElseThrow(() -> new RuntimeException("项目不存在"));
    }

    private void copyRequest(Project project, ProjectRequest request, boolean create) {
        BigDecimal price = request.getPrice();
        BigDecimal deposit = request.getDepositAmount() == null ? BigDecimal.ZERO : request.getDepositAmount();
        BigDecimal balance = request.getBalanceAmount() == null ? price.subtract(deposit) : request.getBalanceAmount();
        if (deposit.compareTo(price) > 0) throw new RuntimeException("定金不能超过项目金额");
        if (balance.compareTo(BigDecimal.ZERO) < 0) throw new RuntimeException("尾款金额不能为负");
        if (deposit.add(balance).compareTo(price) > 0) throw new RuntimeException("定金和尾款不能超过项目金额");
        project.setName(request.getName());
        project.setClient(request.getClient());
        project.setPrice(price);
        project.setDepositAmount(deposit);
        project.setBalanceAmount(balance);
        project.setDepositStatus(normalizePaymentStatus(request.getDepositStatus(), create ? "UNPAID" : project.getDepositStatus()));
        project.setBalanceStatus(normalizePaymentStatus(request.getBalanceStatus(), create ? "UNPAID" : project.getBalanceStatus()));
        project.setContractDate(request.getContractDate() != null ? request.getContractDate() : (create ? LocalDate.now() : project.getContractDate()));
        project.setDueDate(request.getDueDate());
        project.setStatus(request.getStatus() != null && !request.getStatus().isBlank() ? request.getStatus() : (create ? "ACTIVE" : project.getStatus()));
        project.setRemark(request.getRemark());
    }

    private String normalizePaymentStatus(String status, String fallback) {
        if (status == null || status.isBlank()) return fallback;
        if (!"UNPAID".equalsIgnoreCase(status) && !"PARTIAL".equalsIgnoreCase(status) && !"PAID".equalsIgnoreCase(status)) {
            throw new RuntimeException("收款状态必须为 UNPAID、PARTIAL 或 PAID");
        }
        return status.toUpperCase();
    }

    private BigDecimal value(BigDecimal value) { return value == null ? BigDecimal.ZERO : value; }
    private BigDecimal scale(BigDecimal value) { return value.setScale(2, RoundingMode.HALF_UP); }
}
