package com.finance.controller;

import com.finance.common.Result;
import com.finance.dto.ProjectRequest;
import com.finance.entity.Project;
import com.finance.service.ProjectService;
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
@RequestMapping("/api/projects")
public class ProjectController {
    @Autowired
    private ProjectService projectService;

    @GetMapping
    public Result<List<Project>> list() {
        return Result.success(projectService.getAllProjects());
    }

    @GetMapping("/overview")
    public Result<Map<String, Object>> overview() {
        return Result.success(projectService.getOverview());
    }

    @GetMapping("/{id}")
    public Result<Project> detail(@PathVariable Long id) {
        return Result.success(projectService.getProject(id));
    }

    @PostMapping
    public Result<Project> create(@Valid @RequestBody ProjectRequest request) {
        return Result.success(projectService.createProject(request));
    }

    @PutMapping("/{id}")
    public Result<Project> update(@PathVariable Long id, @Valid @RequestBody ProjectRequest request) {
        return Result.success(projectService.updateProject(id, request));
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        projectService.deleteProject(id);
        return Result.success();
    }
}
