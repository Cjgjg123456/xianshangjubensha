package org.example.jubensha.controller;

import org.example.jubensha.common.Result;
import org.example.jubensha.entity.*;
import org.example.jubensha.service.GameService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.io.File;
import java.util.*;

@RestController
@RequestMapping("/api/game")
@CrossOrigin
public class GameController {

    @Autowired private GameService gameService;
    @Value("${file.upload-path}") private String uploadPath;

    @PostMapping("/upload")
    public Result<String> upload(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) return Result.fail("文件为空");
        File dir = new File(uploadPath);
        if (!dir.exists()) dir.mkdirs();
        String fileName = UUID.randomUUID().toString() + "_" + file.getOriginalFilename();
        try {
            file.transferTo(new File(uploadPath + fileName));
            return Result.success("/uploads/" + fileName);
        } catch (Exception e) {
            return Result.fail("上传失败：" + e.getMessage());
        }
    }

    @GetMapping("/scripts")
    public Result<List<Script>> getScripts() { return Result.success(gameService.getScriptList()); }

    @GetMapping("/roles")
    public Result<List<Role>> getRoles(@RequestParam Integer scriptId) {
        return Result.success(gameService.getRolesByScriptId(scriptId));
    }

    @GetMapping("/detail")
    public Result<Map<String, Object>> getDetail(@RequestParam Integer scriptId) {
        return Result.success(gameService.getScriptFullDetail(scriptId));
    }

    @PostMapping("/createScript")
    public Result<Integer> createScript(@RequestBody Map<String, Object> data) {
        return Result.success(gameService.createNewScript(data));
    }

    @PostMapping("/updateScript")
    public Result<Integer> updateScript(@RequestBody Map<String, Object> data) {
        return Result.success(gameService.updateExistingScript(data));
    }

    @PostMapping("/start")
    public Result<GameProgress> startGame(@RequestParam Long userId, @RequestParam Integer scriptId, @RequestParam Integer roleId) {
        try { return Result.success(gameService.startGame(userId, scriptId, roleId)); }
        catch (Exception e) { return Result.fail(e.getMessage()); }
    }

    @GetMapping("/content")
    public Result<Map<String, Object>> getActContent(@RequestParam Integer gameId, @RequestParam(required = false) Integer actId) {
        return Result.success(gameService.getActContentWithCheck(gameId, actId));
    }

    @GetMapping("/clues")
    public Result<List<ScriptClue>> getClues(@RequestParam Integer gameId) {
        return Result.success(gameService.getUnlockedClues(gameId));
    }

    @PostMapping("/finishReading")
    public Result<List<Map<String, String>>> finishReading(@RequestParam Integer gameId) {
        return Result.success(gameService.getAiResponses(gameId));
    }

    @PostMapping("/chat")
    public Result<Map<String, Object>> chat(@RequestBody Map<String, Object> payload) {
        Integer gameId = (Integer) payload.get("gameId");
        List<Map<String, String>> history = (List<Map<String, String>>) payload.get("history");
        return Result.success(gameService.handleAiChat(gameId, history));
    }

    @PostMapping("/nextAct")
    public Result<GameProgress> nextAct(@RequestParam Integer gameId) {
        return Result.success(gameService.nextAct(gameId));
    }

    // ================== 【核心新增：投票结算接口】 ==================
    @PostMapping("/submitVote")
    public Result<Map<String, Object>> submitVote(@RequestBody Map<String, Object> payload) {
        try {
            Integer gameId = Integer.valueOf(payload.get("gameId").toString());
            Integer votedRoleId = Integer.valueOf(payload.get("votedRoleId").toString());
            // votedRoleId = 具体角色的ID, 如果是 0 代表弃票放过
            Map<String, Object> endingData = gameService.submitVote(gameId, votedRoleId);
            return Result.success(endingData);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.fail("投票结算失败：" + e.getMessage());
        }
    }
}